package repositories
import scala.util.{Failure, Success}
import dao.TableMappingsDAO
import dtos.{LikesDTO, PostDataDTO}
import models.{Post, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostRepository @Inject()(protected val dbConfigProvider : DatabaseConfigProvider,
                               tableMappingsDAO: TableMappingsDAO) (implicit ec: ExecutionContext) extends TPostRepository with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  private val posts = tableMappingsDAO.posts

  override def save(post: Post): Future[Option[Post]] = {
    val addAction = (posts returning posts.map(_.postId)) += post
    db.run(addAction.asTry).flatMap {
      case Success(insertedPostId) =>
        val fetchQuery = posts.filter(_.postId === insertedPostId).result.headOption
        db.run(fetchQuery).map {
          case Some(insertedPost) => Some(insertedPost)
          case None =>
            None
        }
      case Failure(_) =>
        Future.successful(None)
    }
  }
  override def delete(post: Post): Future[Boolean] = {
    val deleteAction = posts.filter(_.postId === post.postId).delete
    db.run(deleteAction).map(_>0)
  }

  override def getAll: Future[Seq[Post]] = {
    val query = posts.result
    db.run(query)
  }

  override def getAllByUser(user: User): Future[Seq[Post]] = {
    val query = posts.filter(_.userId === user.userId)
    val result = db.run(query.result)
    result
  }

  override def updatePost(post: Post): Future[Option[Post]] = {
    val query = for {
      _ <- posts.filter(_.postId === post.postId).update(post)
      updated <- posts.filter(_.postId === post.postId).result.headOption
    } yield updated

    db.run(query.transactionally)
  }

  override def getAllByUserId(userId: Long): Future[Seq[Post]] = {
    val query = posts.filter(_.userId === userId)
    db.run(query.result)
  }

  override def isPostByUser(post: Post): Future[Boolean] = {
    val query = posts.filter(p => p.userId === post.userId && p.postId === post.postId).result.headOption
    db.run(query).flatMap{
      case Some(_) => Future.successful(true)
      case None => Future.successful(false)
    }
  }

  override def getPostById(postId: Long): Future[Option[Post]] = {
    val query = posts.filter(_.postId === postId).result.headOption
    db.run(query)
  }

  override def getUserIdByPostId(postId: Long): Future[Long] = {
    val query = posts.filter(_.postId === postId).map(_.userId).result.headOption
    db.run(query).flatMap {
      case Some(userId) => Future.successful(userId)
      case None => Future.failed(new Exception("Post not found"))
    }
  }
}
