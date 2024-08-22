package dtos

import play.api.libs.json.Json

case class PostDataDTO(username : String, postContent : String, likesDTO: LikesDTO, comments : Seq[CommentDataDTO]) {

}

object PostDataDTO{
  implicit val format = Json.format[PostDataDTO]
}
