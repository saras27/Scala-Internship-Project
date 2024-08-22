package models

import play.api.libs.json.{Json, OFormat}


case class User(
                userId : Long,
                username : String,
                password : String,
                profilePicture : Option[String],
                name : Option[String],
                surname : Option[String]
               )
object User{
  implicit val format: OFormat[User] = Json.format[User]
}