package models
import play.api.libs.json.{Json, OFormat}
import models.FriendRequestStatus
import models.FriendRequestStatus.FriendRequestStatus

case class FriendRequest(
                        requestId : Long,
                        userSentId : Long,
                        userGotId : Long,
                        status : FriendRequestStatus.Value
                        )

object FriendRequest{
  implicit val format: OFormat[FriendRequest] = Json.format[FriendRequest]
}