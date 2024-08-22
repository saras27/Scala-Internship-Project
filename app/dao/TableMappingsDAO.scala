package dao

import models.{Comment, FriendRequest, FriendRequestStatus, Like, Message, Post, User}
import models.FriendRequestStatus.FriendRequestStatus

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape


class TableMappingsDAO @Inject()(
                                  protected val dbConfigProvider: DatabaseConfigProvider
                                )(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val friendRequestStatusColumnType: BaseColumnType[FriendRequestStatus.Value] = MappedColumnType.base[FriendRequestStatus.Value, String](
    e => e.toString,
    s => FriendRequestStatus.withName(s)
  )

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def userId = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")
    def profilePicture = column[Option[String]]("profile_picture")
    def name = column[Option[String]]("name")
    def surname = column[Option[String]]("surname")
    def * = (userId, username, password, profilePicture, name, surname) <> ((models.User.apply _).tupled, User.unapply)
  }

  class PostTable(tag : Tag) extends Table[Post](tag, "posts"){
    def postId = column[Long]("post_id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def datePosted = column[Timestamp]("date_posted")
    def content = column[String]("content")
    def * = (postId, userId, datePosted, content) <> ((models.Post.apply _).tupled, Post.unapply)
  }

  class LikesTable(tag : Tag) extends Table[Like](tag, "likes"){
    def userId = column[Long]("user_id")
    def postId = column[Long]("post_id")
    def pk = primaryKey("pk_like", (userId, postId))
    def * = (userId, postId) <> ((models.Like.apply _).tupled, Like.unapply)
  }

  class FriendRequestsTable(tag: Tag) extends Table[FriendRequest](tag, "friend_requests") {
    def requestId = column[Long]("request_id", O.PrimaryKey, O.AutoInc)
    def userSentId = column[Long]("user_sent_id")
    def userGotId = column[Long]("user_got_id")
    def status = column[FriendRequestStatus]("status")

    def * = (requestId, userSentId, userGotId, status) <> ((models.FriendRequest.apply _).tupled, FriendRequest.unapply)
  }

  class CommentsTable(tag : Tag) extends Table[Comment](tag, "comments") {
    def commentId = column[Long]("comment_id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def postId = column[Long]("post_id")
    def comment = column[String]("comment")
    def datePosted = column[Timestamp]("date_posted")

    override def * : ProvenShape[Comment] = (commentId,userId, postId, comment, datePosted) <> ((models.Comment.apply _).tupled, Comment.unapply)
  }

  class MessagesTable(tag : Tag) extends Table[Message](tag, "messages"){
    def messageId = column[Long]("message_id", O.PrimaryKey, O.AutoInc)
    def userFrom = column[Long]("user_from")
    def userTo = column[Long]("user_to")
    def message = column[String]("message")


    override def * : ProvenShape[Message] = (messageId, userFrom, userTo, message) <> ((models.Message.apply _).tupled, Message.unapply)
  }

  val users = TableQuery[UsersTable]
  val posts = TableQuery[PostTable]
  val likes = TableQuery[LikesTable]
  val requests = TableQuery[FriendRequestsTable]
  val comments = TableQuery[CommentsTable]
  val messages = TableQuery[MessagesTable]
}

