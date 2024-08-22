package dtos

import play.api.libs.json.Json

case class CommentDTO(comment : String)

object CommentDTO{
  implicit val format = Json.format[CommentDTO]
}