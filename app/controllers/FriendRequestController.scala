package controllers

import auth.AuthAction
import models.FriendRequestStatus.FriendRequestStatus
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{FriendRequestService, UserService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendRequestController @Inject()(val controllerComponents: ControllerComponents, userService: UserService, friendRequestService: FriendRequestService, authAction: AuthAction)(implicit ec: ExecutionContext) extends BaseController {

  def sendFriendRequest : Action[JsValue] = authAction.async(parse.json){ implicit req =>
    val message: JsValue = req.body
    val userFromId = req.userId
    val userToId = (message \ "userToId").as[Long]

    val requestSent = friendRequestService.addUserAsFriend(userFromId, userToId).flatMap {
      case true => Future.successful(Ok("Friend request sent."))
      case false => Future.successful(BadRequest("Friend request couldn't be sent."))
    }
    requestSent
  }

  def handleFriendRequestAnswer : Action[JsValue] = authAction.async(parse.json){implicit req =>
    val message : JsValue = req.body
    val userFromId = (message \ "userFromId").as[Long]
    val userToId = req.userId
    val answer = (message \ "answer").as[FriendRequestStatus]

    friendRequestService.handleFriendRequest(userFromId, userToId, answer).map {
      case Some(_) => Ok("Friend request handled successfully.")
      case None => BadRequest("Failed to handle friend request.")
    }.recover {
      case e: Exception => InternalServerError(e.getMessage)
    }
  }

  def showUsersFriends: Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId

    userService.getUserById(userId).flatMap {
      case Some(user) =>
        friendRequestService.getAllFriendsAsDTOs(user.userId).map { friends =>
          Ok(Json.toJson(friends))
        }.recover {
          case e: Exception =>
            InternalServerError("Error fetching friends: " + e.getMessage)
        }
      case None =>
        Future.successful(BadRequest("Couldn't find user with provided id."))
    }
  }

}
