package dtos

import models.User
import play.api.libs.json.Json

case class LikesDTO (numLikes : Int, users : Seq[String]){

}

object LikesDTO{
  implicit val format = Json.format[LikesDTO]
}
