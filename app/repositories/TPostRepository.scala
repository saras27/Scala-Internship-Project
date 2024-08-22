package repositories

import models.{Post, User}

import scala.concurrent.Future

trait TPostRepository {
  def save(post : Post) : Future[Option[Post]]
  def delete(post : Post) : Future[Boolean]
  def getAll : Future[Seq[Post]]
  def getAllByUser (user : User) : Future[Seq[Post]]
  def getAllByUserId(userId: Long) : Future[Seq[Post]]
  def updatePost( post : Post) : Future[Option[Post]]
  def isPostByUser( post: Post) : Future[Boolean]
  def getPostById(postId: Long) : Future[Option[Post]]
  def getUserIdByPostId(postId : Long) : Future[Long]
}
