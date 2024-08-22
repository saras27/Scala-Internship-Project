package repositories

import dao.TableMappingsDAO
import models.{Like, Post, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LikeRepository @Inject()(protected val dbConfigProvider : DatabaseConfigProvider,
                               tableMappingsDAO: TableMappingsDAO) (implicit ec: ExecutionContext) extends TLikeRepository with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._
  private val likes = tableMappingsDAO.likes
  private val users = tableMappingsDAO.users
  private val posts = tableMappingsDAO.posts

  override def getAllByPost(post: Post): Future[Seq[User]] = {
    val query = for {
      (like, user) <- likes.filter(_.postId === post.postId) join users on (_.userId === _.userId)
    } yield user

    db.run(query.result)
  }

  override def getAllByUser(user: User): Future[Seq[Post]] ={
    val query = for {
      (like, post) <- likes.filter(_.userId === user.userId) join posts on (_.postId === _.postId)
    } yield post

    db.run(query.result)
  }

  override def add(userId : Long, postId: Long): Future[Boolean] = {
    val like = Like(userId, postId)
    val query = likes += like
    db.run(query).map(_=>true).recover( {case _ => false})
  }

  override def delete(userId : Long, postId : Long): Future[Boolean] = {
    val query = likes.filter(l => l.userId === userId && l.postId === postId).delete
    db.run(query).map(_>0)
  }

  override def checkIfLiked(userId: Long, postId : Long) : Future[Boolean] = {
    val query = likes.filter(like=> like.userId === userId && like.postId === postId)
    db.run(query.result.headOption).map(_.isDefined)
  }

}
