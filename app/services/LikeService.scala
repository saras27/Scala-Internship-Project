package services

import models.FriendRequestStatus.ACCEPTED
import models.{FriendRequestStatus, Post}
import repositories.{FriendRequestRepository, LikeRepository, PostRepository, UserRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LikeService @Inject()(likeRepo: LikeRepository, userRepo : UserRepository, postRepo : PostRepository, friendReqRepo : FriendRequestRepository)(implicit ec : ExecutionContext){

    def likePost(userId : Long, postId : Long) : Future[Boolean] = {
      val postFuture: Future[Option[Post]] = postRepo.getPostById(postId)

      postFuture.flatMap {
        case Some(post) =>
          val postAuthorId = post.userId
          if (userId == postAuthorId )
            likeRepo.add(userId, postId).map(_ => true)
          else {
            friendReqRepo.getRequestStatus(userId, postAuthorId).flatMap {
              case Some(ACCEPTED) =>
                likeRepo.add(userId, postId).map(_ => true)
              case _ =>
                Future.successful(false)
            }
          }
        case None =>
          Future.successful(false)
      }
    }

    def unlikePost(userId : Long, postId : Long) : Future[Boolean] = {
        checkIfLiked(userId, postId).flatMap {
          case true => likeRepo.delete(userId, postId).map(_=> true)
          case false => Future.successful(false)
        }

    }

    private def checkIfLiked(userId : Long, postId : Long) : Future[Boolean] ={
      likeRepo.checkIfLiked(userId, postId)
    }
}
