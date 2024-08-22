package dtos

import play.api.libs.json.Json

case class PasswordChangeFormDTO(oldPassword : String, newPassword : String, newPasswordConfirm : String)


object PasswordChangeFormDTO{
  implicit val format = Json.format[PasswordChangeFormDTO]
}