package services

import dtos.UserDTO
import models.{FriendRequest, FriendRequestStatus, User}
import models.FriendRequestStatus.{ACCEPTED, FriendRequestStatus, PENDING}
import repositories.{FriendRequestRepository, UserRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendRequestService @Inject()(friendRequestRepository: FriendRequestRepository, userRepository: UserRepository)(implicit ec : ExecutionContext){

    def addUserAsFriend(userSentId: Long, userGotId : Long) : Future[Boolean] = {
     userRepository.getUserByUserId(userGotId).flatMap{
       case Some(user) => friendRequestRepository.getRequestStatus(userSentId, userGotId).flatMap {
         case Some(_) => Future.successful(false)
         case _ => friendRequestRepository.save(userSentId, userGotId).map{
           case Some(_) => true
           case None => false
         }
       }
       case None => Future.successful(false)
     }

    }

  def handleFriendRequest(user1 : Long, user2: Long, answer : FriendRequestStatus) : Future[Option[FriendRequest]] = {
    friendRequestRepository.getByUserIds(user1, user2).flatMap{
      case Some(friendRequest) => val status = answer
      status match {
        case FriendRequestStatus.ACCEPTED =>friendRequestRepository.updateStatus(friendRequest.requestId,answer).map(_ => Some(friendRequest))
        case FriendRequestStatus.DECLINED => friendRequestRepository.delete(friendRequest).map(_ => Some(friendRequest))
        case _ => Future.failed(new Exception("Cannot set status to PENDING for an existing friend request"))
      }
      case None => Future.failed(new Exception("No friend request found"))
    }
  }

  def isRequestAccepted(userReqId: Long, userId: Long): Future[Boolean] = {
    if (userReqId == userId) Future.successful(true)
    else{
      friendRequestRepository.getRequestStatus(userReqId, userId).map {
        case Some(status) if (status == FriendRequestStatus.ACCEPTED )=> true
        case _ =>
          if(userId == userReqId) true
          else  false
      }
    }
  }

  def getAllFriendsAsDTOs(userId: Long): Future[Seq[UserDTO]] = {
    friendRequestRepository.getAllAcceptedByUserId(userId).flatMap { friendUserIds =>
      Future.traverse(friendUserIds.flatten) { friendUserId =>
        userRepository.getUserByUserId(friendUserId).flatMap { userOpt =>
          userOpt.map { user =>
            val userDTO = UserDTO(
              username = user.username,
              profilePicture = user.profilePicture,
              name = user.name,
              surname = user.surname,
              posts = None
            )
            Future.successful(userDTO)
          }.getOrElse(Future.successful(UserDTO("Unknown", None, None, None, None)))
        }
      }
    }
  }
}
