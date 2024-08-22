package repositories

import models.Message

import scala.concurrent.Future

trait TMessageRepository {
  def findAllForUser(userId : Long) : Future[Seq[Message]]
  def save(message: Message) : Future[Message]
}
