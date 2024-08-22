package repositories

import dao.TableMappingsDAO
import models.FriendRequestStatus.{ACCEPTED, FriendRequestStatus, PENDING}
import models.{FriendRequest, FriendRequestStatus}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class FriendRequestRepository @Inject()(protected val dbConfigProvider : DatabaseConfigProvider,
                                        tableMappingsDAO: TableMappingsDAO,
                                        userRepository: UserRepository)
                                       (implicit ec: ExecutionContext) extends TFriendRequestRepository with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._
  private val requests = tableMappingsDAO.requests

  override def getAll: Future[Seq[Long]] = {
    val query = requests.map(_.requestId).result
    db.run(query)
  }
  implicit val friendRequestStatusColumnType: BaseColumnType[FriendRequestStatus.Value] =
    MappedColumnType.base[FriendRequestStatus.Value, String](
      e => e.toString,
      s => FriendRequestStatus.withName(s)
    )

  override def save(user1Id : Long, user2Id : Long): Future[Option[FriendRequest]] = {
    val request = FriendRequest(1, user1Id ,user2Id, FriendRequestStatus.PENDING)
    val addAction = (requests returning requests.map(_.requestId)) += request
    db.run(addAction.asTry).flatMap {
      case Success(insertedRequestId) => val insertedRequest = request.copy(requestId = insertedRequestId)
        Future.successful(Some(insertedRequest))
      case Failure(exception) =>
        println(s"Failed to insert friend request: ${exception.getMessage}")
        Future.successful(None)
    }
  }

  override def delete(request: FriendRequest): Future[Boolean] = {
    val deleteAction = requests.filter(_.requestId === request.requestId).delete
    db.run(deleteAction).map(_>0)
  }

  override def updateStatus(friendRequestId: Long, newStatus : FriendRequestStatus): Future[Option[FriendRequest]] = {
    val updateAction = requests.filter(_.requestId === friendRequestId).map(_.status).update(newStatus)

    db.run(updateAction).flatMap{ _ =>
      findById(friendRequestId)
    }
  }

  def findById(id: Long): Future[Option[FriendRequest]] = {
    val query = requests.filter(_.requestId === id).result.headOption
    db.run(query)
  }

  override def getRequestStatus(user1Id: Long, user2Id: Long): Future[Option[FriendRequestStatus]] = {
    val query = requests.filter{ r => (r.userGotId === user1Id && r.userSentId === user2Id) || (r.userGotId === user2Id && r.userSentId === user1Id) }
    db.run(query.result).map{ result =>if(result.isEmpty) None
                                        else { val first = result.head
                                               Some(first.status)} }
  }

  override def getAllFriendsByUserId(userId: Long): Future[Seq[Option[Long]]] = {
    val query = requests.filter(f => (f.userSentId === userId || f.userGotId === userId) && f.status===ACCEPTED).result
    db.run(query).flatMap { friendRecords =>
      val friendIds = friendRecords.flatMap { friendRecord =>
        List(friendRecord.userSentId, friendRecord.userGotId)
      }.distinct

      val userFuture = Future.traverse(friendIds) { friendId =>
        userRepository.getUserByUserId(friendId).map(_.map(_.userId))
      }
      userFuture
    }
  }
  override def getAllAcceptedByUserId(userId: Long): Future[Seq[Option[Long]]] = {
    val query = requests.filter { f =>
      ((f.userSentId === userId && f.userGotId =!= userId) || (f.userGotId === userId && f.userSentId =!= userId)) &&
        f.status === ACCEPTED
    }.result

    db.run(query).flatMap { friendRecords =>
      val friendIds = friendRecords.flatMap { friendRecord =>
        List(friendRecord.userSentId, friendRecord.userGotId)
      }.distinct.filterNot(_ == userId)

      Future.traverse(friendIds) { friendId =>
        userRepository.getUserByUserId(friendId).map(_.map(_.userId))
      }
    }
  }

  override def getAllPendingByUserId(userId: Long): Future[Seq[Option[FriendRequest]]] = {
    val query = requests.filter(f => (f.userGotId === userId) && f.status===PENDING).result
    db.run(query).map(_.map(Option(_)))
  }

  override def getByUserIds(user1Id: Long, user2Id: Long): Future[Option[FriendRequest]] = {
    val query = requests.filter( r => (r.userGotId === user1Id && r.userSentId === user2Id) || (r.userGotId === user2Id && r.userSentId === user1Id)).result.headOption
    db.run(query)
  }
}
