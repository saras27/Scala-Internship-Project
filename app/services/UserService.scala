package services

import auth.{AuthService, EmailAuth}
import dtos.{ForgotPasswordDTO, PasswordChangeFormDTO, RegisterUserDTO, ResetPasswordDTO, UserDTO}
import models.{CustomException, Post, User}
import org.mindrot.jbcrypt.BCrypt
import repositories.{FriendRequestRepository, PostRepository, UserRepository}

import java.nio.file.{Paths, Files => JFiles}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class UserService @Inject()(userRepo : UserRepository, postRepo : PostRepository, friendReqRepo : FriendRequestRepository,
                            postService: PostService, friendRequestService: FriendRequestService, emailService: EmailService, emailAuth: EmailAuth)(implicit ec : ExecutionContext){


      private val imagesDirectory = "/home/student/Desktop/pokusaj/social-network/public/images"

      def registerUser(userDTO : RegisterUserDTO) : Future[Boolean] = {
            val user = User(123, userDTO.username, userDTO.password, Some("user-placeholder.jpg"),None,None)
            userRepo.getByUsername(user.username).flatMap {
                  case None => userRepo.save(user).map{
                        case true => true
                        case false => false
                  }.recover {
                        case ex: Exception =>
                              println(s"Error saving user: ${ex.getMessage}")
                              ex.printStackTrace()
                              false
                  }
                  case Some(_) => Future.successful(false)
            }.recover {
                  case ex: Exception =>
                        println(s"Error checking username: ${ex.getMessage}")
                        ex.printStackTrace()
                        false
            }
      }

      def validateUserOnLogin(username : String, password: String): Future[Option[User]] = {
            val userFuture = userRepo.getByUsername(username)
            userFuture.map{
                  case Some(user) if checkPassword(password, user.password) => Some(user)
                  case _ => None
            }.recover {
                  case _: Throwable => None
            }
      }

      private def checkPassword(password: String, pswFromDB : String): Boolean = {
            if (BCrypt.checkpw(password, pswFromDB)) {
                  true
            }
            else{
                  false
            }
      }

      def getPostsForUserId(userId: Long): Future[Seq[Post]] ={

            val friendIdsFuture : Future[Seq[Option[Long]]] = friendReqRepo.getAllFriendsByUserId(userId)
            friendIdsFuture.flatMap{friendsIds =>
                  val validFriendIds = friendsIds.flatten

                  val usersFuture: Future[Seq[Option[User]]] = Future.sequence(validFriendIds.map(userRepo.getUserByUserId))

                  usersFuture.flatMap { usersOption =>
                        val validUsers = usersOption.flatten
                        val postsFutures: Seq[Future[Seq[Post]]] = validUsers.map { user =>
                              postRepo.getAllByUserId(user.userId)
                        }
                        Future.sequence(postsFutures)
                  }
            }.map(_.flatten)
      }

      def getAllUsers : Future[Seq[User]] = {
           userRepo.getAll
      }

      def showSearchResults(character: String) : Future[Seq[UserDTO]] = {
            searchForUser(character).map { users =>
                  users.map { user =>
                        UserDTO(
                              username = user.username,
                              profilePicture = user.profilePicture,
                              name = user.name,
                              surname = user.surname,
                              posts = None
                        )
                  }
            }
      }

      private def searchForUser(character: String) : Future[Seq[User]] = {
            userRepo.searchForUser(character)
      }

      def getUserDetails(userId : Long, userReqId : Long) : Future[Option[UserDTO]] = {
            userRepo.getUserByUserId(userId).flatMap {
                  case Some(user) =>
                        friendRequestService.isRequestAccepted(userReqId, userId).flatMap { isAccepted =>
                              if (isAccepted || userId == userReqId) {
                                    postService.getAllForPost(userId).map { postDataDTO =>
                                          Some(UserDTO(user.username, user.profilePicture, user.name, user.surname, Some(postDataDTO)))
                                    }
                              } else {
                                    Future.successful(Some(UserDTO(user.username, user.profilePicture, user.name, user.surname, None)))
                              }
                        }
                  case None =>
                        Future.successful(None)
            }
      }

      def getUserById(userId: Long): Future[Option[User]] = {
            userRepo.findById(userId).map(_.headOption)
      }

      def updateProfile(userId : Long, name : Option[String], surname: Option[String] , password : Option[String]/* , picture : Option[String]*/) : Future[Option[UserDTO]] = {
            userRepo.getUserByUserId(userId).flatMap{
                  case Some(user) =>
                        val updatedUser = user.copy(name = name.orElse(user.name), surname = surname.orElse(user.surname), password = password.getOrElse(user.password)/*, profilePicture = picture.orElse(user.profilePicture)*/)
                        userRepo.update(updatedUser).flatMap {
                              case true => getUserDetails(userId, userId)
                              case false => Future.successful(None)
                        }
                  case None => Future.successful(None)
            }
      }

      def updateProfilePicture( userId : Long, picturePath : String) : Future[Boolean] ={
            userRepo.getUserByUserId(userId).flatMap {
                  case Some(user) =>
                        val oldPicture = user.profilePicture.getOrElse("user-placeholder.jpg")
                        val oldPicturePath = Paths.get(imagesDirectory, oldPicture)

                        if (oldPicture != "user-placeholder.jpg") {
                              JFiles.delete(oldPicturePath)
                        }

                        userRepo.updateProfilePicture(userId, picturePath)
                  case None =>
                        Future.successful(false)
            }
      }
      def changeUserPassword(userId: Long, form: PasswordChangeFormDTO): Future[Option[UserDTO]] = {
            getUserById(userId).flatMap {
                  case Some(user) =>
                        validateUserOnLogin(user.username, form.oldPassword).flatMap {
                              case Some(user) =>
                                    if (form.newPassword == form.newPasswordConfirm) {
                                          changePassword(user.userId, form.newPassword)
                                    } else {
                                          Future.successful(None)
                                    }
                              case None => Future.successful(None)
                        }
                  case None => Future.successful(None)
            }
      }

      def changePassword(userId : Long, newPassword: String) : Future[Option[UserDTO]] ={
            val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
            updateProfile(userId, None, None, Some(hashedPassword))
      }

      def forgotPassword(userData : ForgotPasswordDTO) : Future[Unit] = {
            userRepo.getByUsername(userData.username).flatMap{
                 case Some(user) =>
                       val token = emailAuth.generateForPassword(user.userId)
                       emailService.SendResetPassword(userData.email, token)
                       Future.successful(())
                 case None => Future.failed(new Exception("User not found"))
            }
      }

      def resetPassword(resetPasswordDTO: ResetPasswordDTO, token: String): Future[Boolean] = {
            emailAuth.validatePasswordToken(token).flatMap{ claim =>
                  val userId: Long = claim.subject.getOrElse(throw new CustomException("Invalid token subject")).toLong
                  getUserById(userId).flatMap {
                        case Some(user) =>
                              if (resetPasswordDTO.newPassword == resetPasswordDTO.confirmPassword) {
                                    changePassword(userId, resetPasswordDTO.newPassword).map {
                                          case Some(_) => true
                                          case None => throw new CustomException("Password change failed")
                                    }
                              } else {
                                    Future.failed(new CustomException("Passwords do not match"))
                              }
                        case None =>
                              Future.failed(new CustomException("User not found"))
                  }
            }.recover {
                  case ex: Exception =>
                        println(s"Error resetting password: ${ex.getMessage}")
                        ex.printStackTrace()
                        false // Return false for any exception
            }
      }
}
