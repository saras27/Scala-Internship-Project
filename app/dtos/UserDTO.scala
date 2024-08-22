package dtos

import play.api.libs.json.{JsValue, Json, Writes}

case class UserDTO(username : String, profilePicture : Option[String], name : Option[String], surname : Option[String], posts : Option[Seq[PostDataDTO]])

object UserDTO {
  implicit val writes: Writes[UserDTO] = new Writes[UserDTO] {
    def writes(userDTO: UserDTO): JsValue = Json.obj(
      "username" -> userDTO.username,
      "profilePicture" -> userDTO.profilePicture,
      "name" -> userDTO.name,
      "surname" -> userDTO.surname,
      "posts" -> userDTO.posts
    )
  }
}

