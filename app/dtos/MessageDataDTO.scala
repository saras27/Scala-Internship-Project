package dtos

import play.api.libs.json.Json

case class MessageDataDTO(usernameFrom : String, usernameTo : String, message : MessageDTO)

object MessageDataDTO{
  implicit val format = Json.format[MessageDataDTO]
}