package dtos

import play.api.libs.json.Json

case class ResetPasswordDTO(newPassword : String, confirmPassword : String)

object ResetPasswordDTO{
  implicit val format = Json.format[ResetPasswordDTO]
}