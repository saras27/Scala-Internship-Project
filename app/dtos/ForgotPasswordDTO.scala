package dtos

import play.api.libs.json.Json

case class ForgotPasswordDTO(username : String, email : String)
object ForgotPasswordDTO{
  implicit val format = Json.format[ForgotPasswordDTO]
}