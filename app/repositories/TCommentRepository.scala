package repositories

import models.Comment

import scala.concurrent.Future

trait TCommentRepository {
  def getAllForPost(postId : Long) : Future[Seq[Comment]]
  def save(comment: Comment) : Future[Option[Comment]]
  def delete(comment: Comment) : Future[Boolean]
  def findById(commentId : Long) : Future[Option[Comment]]
}
