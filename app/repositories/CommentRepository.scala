package repositories

import dao.TableMappingsDAO
import models.Comment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CommentRepository @Inject()(protected val dbConfigProvider : DatabaseConfigProvider,
                                   tableMappingsDAO: TableMappingsDAO)
                                 (implicit ec: ExecutionContext) extends TCommentRepository with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._
  private val comments = tableMappingsDAO.comments

  override def getAllForPost(postId: Long): Future[Seq[Comment]] = {
    val query = comments.filter(_.postId === postId).result
    db.run(query)
  }

  override def save(comment: Comment): Future[Option[Comment]] = {
    val addAction = (comments returning comments.map(_.commentId)) += comment
    db.run(addAction.asTry).flatMap {
      case Success(insertedCommentId) =>
        val fetchQuery = comments.filter(_.commentId === insertedCommentId).result.headOption
        db.run(fetchQuery).map {
          case Some(insertedComment) => Some(insertedComment)
          case None =>
            None
        }
      case Failure(_) =>
        Future.successful(None)
    }
  }

  override def delete(comment: Comment): Future[Boolean] = {
    val deleteAction = comments.filter(_.commentId === comment.commentId).delete
    db.run(deleteAction).map(_>0)
  }

  override def findById(commentId: Long): Future[Option[Comment]] = {
    val query = comments.filter(_.commentId === commentId).result.headOption
    db.run(query)
  }

}
