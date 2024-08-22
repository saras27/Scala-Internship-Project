package dtos

import play.api.libs.json.Json

case class CommentDataDTO(username : String, comment : String)

object CommentDataDTO{
  implicit val format = Json.format[CommentDataDTO]
}