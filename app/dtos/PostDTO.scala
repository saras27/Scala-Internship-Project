package dtos

import play.api.libs.json.Json

case class PostDTO (content : String)

object PostDTO{
  implicit val format = Json.format[PostDTO]
}
