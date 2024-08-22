package repositories

import models.{Like, Post, User}

import scala.concurrent.Future

trait TLikeRepository {
      def getAllByPost(post : Post) : Future[Seq[User]]
      def getAllByUser(user : User) : Future[Seq[Post]]
      def add(userId : Long, postId : Long) : Future[Boolean]
      def delete(userId : Long, postId : Long) : Future[Boolean]
      def checkIfLiked(userId: Long, postId : Long) : Future[Boolean]
}
