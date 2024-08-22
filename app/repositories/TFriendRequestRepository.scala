package repositories

import models.FriendRequestStatus.FriendRequestStatus
import models.{FriendRequest, User}

import scala.concurrent.Future

trait TFriendRequestRepository {
  def getAll : Future[Seq[Long]]
  def save(user1Id : Long, user2Id : Long) : Future[Option[FriendRequest]]
  def delete(request: FriendRequest) : Future[Boolean]
  def updateStatus(friendRequestId: Long, newStatus : FriendRequestStatus) : Future[Option[FriendRequest]]
  def getRequestStatus(user1Id : Long, user2Id : Long) : Future[Option[FriendRequestStatus]]
  def getAllFriendsByUserId(userId : Long) : Future[Seq[Option[Long]]]
  def getByUserIds(user1Id: Long, user2Id: Long) : Future[Option[FriendRequest]]
  def getAllPendingByUserId(userId: Long): Future[Seq[Option[FriendRequest]]]
  def findById(id: Long): Future[Option[FriendRequest]]
  def getAllAcceptedByUserId(userId: Long): Future[Seq[Option[Long]]]
}
