package models
import play.api.libs.json.{Json, OFormat}
case class Like(
               userId : Long,
               postId : Long
               )

object Like{
  implicit val format: OFormat[Like] = Json.format[Like]
}
