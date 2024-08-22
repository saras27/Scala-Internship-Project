package controllers

import auth.{AuthAction, AuthService}
import dtos.{ForgotPasswordDTO, PasswordChangeFormDTO, RegisterUserDTO, ResetPasswordDTO, UserDTO}
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.Files
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.UserService

import java.nio.file.Paths
import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, authAction : AuthAction, authService: AuthService)(implicit ec: ExecutionContext) extends BaseController {


  def login: Action[JsValue] = Action.async(parse.json) { implicit req =>
    val message: JsValue = req.body
    val username = (message \ "username").as[String]
    val password = (message \ "password").as[String]

    val userFuture = userService.validateUserOnLogin(username, password)

    userFuture.map {
      case Some(validUser) =>
        val userId = validUser.userId
        val token = authService.encode(userId)

        showPosts(userId)

        Ok(Json.toJson("token" -> token))
      case None =>
        Unauthorized("Invalid credetials")
    }

  }

  def register: Action[JsValue] = Action.async(parse.json) { implicit req =>
    req.body.validate[RegisterUserDTO].fold(
      errors => {
        Future.successful(BadRequest("Invalid JSON"))
      },
      userData => {
        val hashedPassword = BCrypt.hashpw(userData.password, BCrypt.gensalt())
        val userWithHashedPassword = userData.copy(password = hashedPassword)

        userService.registerUser(userWithHashedPassword).map {
          case true => Ok("User registered successfully")
          case false => BadRequest("Username already exists")
        }
      }
    )
  }

  private def showPosts(userId: Long): Future[Result] = {
    val postsFuture = userService.getPostsForUserId(userId)
    postsFuture.map { posts =>
      Ok(Json.toJson("posts" -> posts))
    }
  }

  def showAllUsers: Action[AnyContent] = Action.async {
    userService.getAllUsers.map { usersSeq =>
      Ok(Json.toJson(usersSeq))
    }
  }

  def searchUsers: Action[AnyContent] = authAction.async { implicit request =>
    request.body.asJson.map { json =>
      val username = (json \ "username").as[String]
      userService.showSearchResults(username).map { users =>
        if (users.nonEmpty) {
          Ok(Json.toJson(users))
        } else {
          NotFound(Json.obj("message" -> "User not found"))
        }
      }.recover {
        case e: Exception =>
          InternalServerError("Error searching for users")
      }
    }.getOrElse {
      Future.successful(BadRequest(Json.obj("message" -> "Expecting application/json request body")))
    }
  }

  def showUserProfile(userId: Long): Action[AnyContent] = authAction.async { implicit request =>
    userService.getUserById(request.userId).flatMap {
      case Some(user) =>
        userService.getUserDetails(userId, user.userId).map {
          case Some(userDTO) =>
            Ok(Json.toJson(userDTO))
          case None =>
            NotFound(Json.obj("message" -> "User not found"))
        }.recover {
          case e: Exception =>
            InternalServerError("Error searching for users")
        }
    }
  }

  def updateUserInformation: Action[JsValue] = authAction.async(parse.json) { implicit request =>
    val userId = request.userId
    val info: JsValue = request.body

    val name = (info \ "name").asOpt[String]
    val surname = (info \ "surname").asOpt[String]

    userService.updateProfile(userId, name, surname, None).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound(Json.obj("message" -> "User not found or update failed"))
    }

  }

  def uploadPicture: Action[MultipartFormData[Files.TemporaryFile]] = authAction.async(parse.multipartFormData) { implicit request =>
    val userId = request.userId

    request.body.file("picture").map { picture =>
      val originalFilename = Paths.get(picture.filename).getFileName.toString
      val fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
      val uniqueHash = Base64.getEncoder.encodeToString(originalFilename.getBytes) + System.currentTimeMillis()
      val newFilename = s"${originalFilename.dropRight(fileExtension.length + 1)}$uniqueHash.$fileExtension"
      val pictureDir = Paths.get("/home/student/Desktop/pokusaj/social-network/public/images")
      val picturePath = pictureDir.resolve(newFilename)

      picture.ref.moveTo(picturePath, replace = true)
      val pictureUrl = s"$newFilename"


      userService.updateProfilePicture(userId, pictureUrl).map { success =>
        if (success) {
          Ok("File uploaded and profile picture updated")
        } else {
          InternalServerError("File uploaded but failed to update profile picture")
        }
      }
    }.getOrElse {
      Future.successful(BadRequest("Missing file"))
    }
  }
  
  def changePassword : Action[JsValue] = authAction.async(parse.json) { implicit request =>
    val userId = request.userId
    val passwordChangeForm = request.body.validate[PasswordChangeFormDTO]

    passwordChangeForm match {
      case JsSuccess(form, _) =>
        userService.changeUserPassword(userId, form).map {
          case Some(_) => Ok("Password changed successfully")
          case None => InternalServerError("Failed to process password change")
        }
      case JsError(errors) =>
        Future.successful(BadRequest(errors.toString()))
    }
  }

  def forgotPassword : Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[ForgotPasswordDTO].fold(
      errors => {
        Future.successful(BadRequest("Invalid JSON"))
      },
      userData => {
        userService.forgotPassword(userData).map { _ =>
          Ok("Reset password email sent")
        }.recover {
          case e: Exception => BadRequest(e.getMessage)
        }
      }
    )
  }

  def resetPassword(token:String) : Action[JsValue] = Action.async(parse.json){ implicit request =>
    request.body.validate[ResetPasswordDTO].fold(
      errors => {
        Future.successful(BadRequest("Invalid JSON"))
      },
      userData => {
        userService.resetPassword(userData, token).map{
          case true => Ok("Password reset successful")
          case false => BadRequest("Password reset failed")
        }
      }
    )

  }
}
