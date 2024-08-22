package controllers

import auth.AuthAction
import dtos.MessageDTO
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{MessageService, UserService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MessageController @Inject()(val controllerComponents: ControllerComponents, userService : UserService, messageService : MessageService, authAction: AuthAction)(implicit ec: ExecutionContext) extends BaseController {

  def sendMessage(userToId : Long) : Action[JsValue] = authAction.async(parse.json){ implicit request =>
    val message = request.body.validate[MessageDTO]
    val userFromId = request.userId

    message.fold(
      errors => Future.successful(BadRequest(errors.toString)),
      messageContent => {
        messageService.sendMessage(messageContent, userFromId, userToId).map { messageData =>
          Ok(Json.toJson(messageData))
        }.recover {
          case e: Exception =>
            InternalServerError("An error occurred: " + e.getMessage)
        }
      }
    )
  }

  def showAllMessages : Action[AnyContent] = authAction.async{ implicit request =>
    val userId = request.userId

    userService.getUserById(userId).flatMap {
      case Some(user) => messageService.getAllMessages(userId).map { messages =>
        Ok(Json.toJson(messages))
      }.recoverWith{
        case e : Exception => Future.successful(InternalServerError("Error fetching messages by friends"))
      }
      case None => Future.successful(BadRequest("Couldn't find user with provided id."))
    }

  }

}
