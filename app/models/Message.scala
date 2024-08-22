package models

import play.api.libs.json.{Json, OFormat}

case class Message(messageId : Long,
                   userFrom : Long,
                   userTo : Long,
                   message: String)

object Message{
  implicit val format: OFormat[Message] = Json.format[Message]
}