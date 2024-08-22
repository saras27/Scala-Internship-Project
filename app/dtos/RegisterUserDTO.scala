package dtos

import play.api.libs.json.{JsValue, Json, Writes}

case class RegisterUserDTO(username : String, password : String)
object RegisterUserDTO{
  implicit val format = Json.format[RegisterUserDTO]
}