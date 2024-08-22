package services

import dtos.{LikesDTO, PostDTO, PostDataDTO, UserDTO}
import models._
import repositories.{CommentRepository, FriendRequestRepository, LikeRepository, PostRepository, UserRepository}

import java.sql.Timestamp
import java.time.Instant.now
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostService @Inject()(postRepo : PostRepository, friendRepo : FriendRequestRepository, userRepo: UserRepository, likeRepo : LikeRepository, commentService: CommentService)(implicit ec : ExecutionContext){

  def submitPost(userId : Long, postDTO : PostDTO): Future[PostDataDTO] = {
    val post = Post(123, userId, Timestamp.from(now), postDTO.content)
    postRepo.save(post).flatMap {
      case Some(post) => toPostDataDTO(post)
      case None    => Future.failed(new Exception("Failed to save post"))
    }
  }

  def updatePost(postId : Long, postDTO : PostDTO, userId : Long): Future[PostDataDTO] = {
    postRepo.getPostById(postId).flatMap{
      case Some(post) if post.userId ==  userId =>
        val updatedPost = post.copy(content = postDTO.content)
        postRepo.updatePost(updatedPost).flatMap {
          case Some(updatedPost) => toPostDataDTO(updatedPost)
          case None    => Future.failed(new Exception("Failed to save post"))
        }
      case None => Future.failed(new Exception("Failed to find post to update"))
    }
  }

  def showByFriends(userId : Long) : Future[Seq[PostDataDTO]] ={
    friendRepo.getAllFriendsByUserId(userId).flatMap { friendIds =>
      val nonEmptyFriendIds = friendIds.flatten

      Future.traverse(nonEmptyFriendIds) { friendId =>
        getAllForPost(friendId)
      }.map(_.flatten)
    }
  }

  def showTopPosts(userId: Long) : Future[Seq[PostDataDTO]] = {
    showByFriends(userId).map{ posts =>
      val sortedPosts = posts.sortBy(post => (post.likesDTO.numLikes, post.comments.size)).reverse.take(3)
      sortedPosts
      }
  }

  def getAllForPost(friendId: Long): Future[Seq[PostDataDTO]] = {
    postRepo.getAllByUserId(friendId).flatMap { posts =>
      val sortedPosts = posts.sortBy(_.datePosted).reverse
      Future.traverse(sortedPosts) { post =>
        toPostDataDTO(post)
      }
    }
  }

  private def toPostDataDTO(post: Post) = {
    for {
      postUser <- userRepo.getUserByUserId(post.userId)
      likeUsers <- likeRepo.getAllByPost(post)
      comments <- commentService.getAllForPost(post.postId)
      commentDTOs <- commentService.convertToDTO(comments)
    } yield {
      val username = postUser.map(_.username).getOrElse("Unknown")
      val likeUsernames = likeUsers.map(_.username)
      val likesDTO = LikesDTO(likeUsers.size, likeUsernames)
      PostDataDTO(username, post.content, likesDTO, commentDTOs)
    }
  }

}
