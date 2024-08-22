package controllers

import auth.AuthAction
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.LikeService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LikeController @Inject()(val controllerComponents: ControllerComponents, likeService : LikeService, authAction: AuthAction)(implicit ec: ExecutionContext) extends BaseController {

  def likePost(postId: Long) : Action[AnyContent] = authAction.async{ implicit req =>
    val userId = req.userId
    likeService.likePost(userId, postId).map{
      case true => Ok("Post liked successfully")
      case false => BadRequest("Something went wrong")
    }
  }

  def unlikePost(postId: Long) : Action[AnyContent] = authAction.async{ implicit req =>
    val userId = req.userId

    likeService.unlikePost(userId, postId).map{
      case true => Ok("Like removed successfully")
      case false => BadRequest("Something went wrong")
    }
  }
}
