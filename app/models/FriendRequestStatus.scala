package models
import play.api.libs.json._

object FriendRequestStatus extends Enumeration{
  type FriendRequestStatus = Value
  val ACCEPTED, PENDING, DECLINED =  Value

  implicit val format: Format[FriendRequestStatus] = new Format[FriendRequestStatus.Value] {
    override def writes(status: FriendRequestStatus.Value): JsValue = Json.toJson(status.toString)
    override def reads(json: JsValue): JsResult[FriendRequestStatus.Value] = json.validate[String].map(FriendRequestStatus.withName)
  }
}

