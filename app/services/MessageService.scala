package services

import dtos.{MessageDTO, MessageDataDTO}
import models.Message
import repositories.{FriendRequestRepository, MessageRepository, UserRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MessageService @Inject()(friendRequestService: FriendRequestService, userRepository: UserRepository, messageRepository: MessageRepository)(implicit ec : ExecutionContext){

  def sendMessage(messageDTO : MessageDTO, userFromId : Long, userToId: Long) : Future[MessageDataDTO] = {
    friendRequestService.isRequestAccepted(userToId, userFromId).flatMap{
      case true =>
        val message = Message(123, userFrom = userFromId, userTo = userToId, message = messageDTO.message)
        messageRepository.save(message).flatMap { savedMessage =>
          convertToDTO(messageDTO, userFromId, userToId)
        }
      case false => Future.failed(new Exception("Failed to send message"))
    }
  }

  private def convertToDTO(messageDTO: MessageDTO, userFromId: Long, userToId: Long): Future[MessageDataDTO] = {
    for {
      usernameFrom <- userRepository.getUsernameByUserId(userFromId)
      usernameTo <- userRepository.getUsernameByUserId(userToId)
    } yield {
      val usernameFromStr = usernameFrom.getOrElse("Unknown")
      val usernameToStr = usernameTo.getOrElse("Unknown").toString
      MessageDataDTO(usernameFrom = usernameFromStr, usernameTo = usernameToStr, message = messageDTO)
    }
  }

  def getAllMessages(userId : Long) : Future[Seq[MessageDataDTO]] = {
    userRepository.getUserByUserId(userId).flatMap{
      case Some(user) =>
        messageRepository.findAllForUser(user.userId).flatMap { messages =>
          val messageDataDTOFutures = messages.map { message =>
            val messageDTO = MessageDTO(message.message)
            convertToDTO(messageDTO, message.userFrom, message.userTo)
          }
          Future.sequence(messageDataDTOFutures)
        }.recoverWith {
          case ex => Future.failed(new Exception(s"Failed to find messages for user ${user.userId}", ex))
        }
      case None => Future.failed(new Exception("Failed to find user"))
    }
  }
}
