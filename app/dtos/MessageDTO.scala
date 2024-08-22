package dtos

import play.api.libs.json.Json

case class MessageDTO(message : String)

object MessageDTO{
  implicit val format = Json.format[MessageDTO]
}