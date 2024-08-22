package services

import dtos.{CommentDataDTO, PostDataDTO}
import models.{Comment, Post}
import repositories.{CommentRepository, PostRepository, UserRepository}

import java.sql.Timestamp
import java.time.Instant.now
import java.util.function.ToDoubleFunction
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentService @Inject()( commentRepo : CommentRepository, friendRequestService: FriendRequestService, postRepo : PostRepository, userRepo : UserRepository)(implicit ec : ExecutionContext){

  def submitComment( userCommentingId : Long, comment :String , postId : Long) : Future[CommentDataDTO] = {
    postRepo.getUserIdByPostId(postId).flatMap{
      case userId : Long =>
        friendRequestService.isRequestAccepted(userId, userCommentingId).flatMap{
          case true =>
            val commented = Comment(123, userCommentingId, postId = postId, comment = comment, datePosted = Timestamp.from(now))
            commentRepo.save(commented).flatMap{
              case Some(newComment) => singleCommentToDTO(newComment)
              case None => Future.failed(new Exception("Failed to save comment"))
            }
          case _ => Future.failed(new Exception("Cannot comment on another user's post"))
        }
      case _ =>
        Future.failed(new Exception("Cannot find user or user's post"))
    }
  }

  def getAllForPost(postId : Long) : Future[Seq[Comment]] = {
    postRepo.getPostById(postId).flatMap {
      case Some(post) =>
        commentRepo.getAllForPost(postId)
      case None =>
        Future.successful(Seq.empty[Comment])
    }
  }

  def convertToDTO (comments : Seq[Comment]) : Future[Seq[CommentDataDTO]] = {
    Future.sequence {
      comments.map { comment =>
        singleCommentToDTO(comment)
      }
    }
  }


  private def singleCommentToDTO(comment: Comment) = {
    userRepo.getUsernameByUserId(comment.userId).map { username =>
      CommentDataDTO(
        username = username.getOrElse("Unknown"),
        comment = comment.comment
      )
    }
  }
}
