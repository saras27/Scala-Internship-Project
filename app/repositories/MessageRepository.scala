package repositories

import dao.TableMappingsDAO
import models.Message
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class MessageRepository @Inject() (protected val dbConfigProvider : DatabaseConfigProvider,
tableMappingsDAO: TableMappingsDAO) (implicit ec: ExecutionContext) extends TMessageRepository with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  private  val messages = tableMappingsDAO.messages

  override def findAllForUser(userId: Long): Future[Seq[Message]] = {
    val query = messages.filter(_.userTo === userId)
    val result = db.run(query.result)
    result
  }

  override def save(message: Message): Future[Message] = {
    val addAction = (messages returning messages.map(_.messageId) into ((message, messageId) => message.copy(messageId = messageId))) += message
    db.run(addAction)
  }

}
