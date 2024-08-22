package controllers

import auth.AuthAction
import dtos.CommentDTO
import models.Comment
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.CommentService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentController  @Inject()(val controllerComponents: ControllerComponents, commentService: CommentService, authAction: AuthAction)(implicit ec: ExecutionContext) extends BaseController {

  def submitComment (postId : Long): Action[JsValue] = authAction.async(parse.json){ implicit request =>
    val info = request.body.validate[CommentDTO]
    val userId = request.userId
    info.fold(
      errors => Future.successful(BadRequest(errors.toString)),
      comment => {
        commentService.submitComment(userId, comment.comment, postId).map { commentDTO =>
          Ok(Json.toJson(commentDTO))
        }.recover {
          case e: Exception =>
            InternalServerError("An error occurred: " + e.getMessage)
        }
      }
    )
  }
}
