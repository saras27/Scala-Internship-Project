package controllers

import auth.AuthAction
import dtos.PostDTO
import models._
import play.api.mvc._
import services.{PostService, UserService}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import slick.ast.Library.User

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostController @Inject()(val controllerComponents: ControllerComponents, postService: PostService,userService : UserService,  authAction: AuthAction)(implicit ec: ExecutionContext) extends BaseController {

  def createPost : Action[JsValue] = authAction.async(parse.json){ implicit req =>
    val message = req.body.validate[PostDTO]
    val userId = req.userId

    message.fold(
      errors => Future.successful(BadRequest(errors.toString)),
      postContent => {
        postService.submitPost(userId, postContent).map { postDataDTO =>
          Ok(Json.toJson(postDataDTO))
        }.recover {
          case e: Exception =>
            InternalServerError("An error occurred: " + e.getMessage)
        }
      }
    )
  }

  def editPost(postId : Long) : Action[JsValue] = authAction.async(parse.json) { implicit req =>
    req.body.validate[PostDTO].fold(
      errors => {
        Future.successful(BadRequest(errors.toString))
      },
      postDTO => {
        postService.updatePost(postId, postDTO, req.userId).map {
          postDataDTO => Ok(Json.toJson(postDataDTO))
        }.recover {
          case e: Exception =>
            BadRequest("An error occurred: " + e.getMessage)
        }
      }
    )
  }

  def showByFriends : Action[AnyContent] = authAction.async { implicit req =>
    val userIdResult = req.userId

    userService.getUserById(userIdResult).flatMap {
      case Some(user) => postService.showByFriends(user.userId).map { posts =>
        Ok(Json.toJson(posts))
      }.recoverWith{
        case e : Exception => Future.successful(InternalServerError("Error fetching posts by friends"))
      }
      case None => Future.successful(BadRequest("Couldn't find user with provided id."))
    }
  }

  def showTopPosts : Action[AnyContent] = authAction.async{ implicit request =>
    val userId = request.userId

    userService.getUserById(userId).flatMap {
      case Some(user) => postService.showTopPosts(userId).map { posts =>
        Ok(Json.toJson(posts))
      }.recoverWith{
        case e : Exception => Future.successful(InternalServerError("Error fetching posts by friends"))
      }
      case None => Future.successful(BadRequest("Couldn't find user with provided id."))
    }
  }
}
