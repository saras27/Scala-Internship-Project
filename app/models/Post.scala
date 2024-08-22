package models

import org.apache.pekko.http.scaladsl.model.DateTime
import play.api.libs.json.{Format, JsResult, JsValue, Json, OFormat}

import java.sql.Timestamp

case class Post(
                 postId : Long,
                 userId : Long,
                 datePosted : Timestamp,
                 content : String
               )

object Post{
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    override def writes(timestamp: Timestamp): JsValue = Json.toJson(timestamp.getTime)
    override def reads(json: JsValue): JsResult[Timestamp] = json.validate[Long].map(new Timestamp(_))
  }
  implicit val format: OFormat[Post] = Json.format[Post]
}