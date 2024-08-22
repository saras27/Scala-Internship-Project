package repositories
import play.api.db.slick.HasDatabaseConfigProvider
import dao.TableMappingsDAO
import models.User
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import dao.TableMappingsDAO
import play.api.db.slick.DatabaseConfigProvider
import repositories.TUserRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class UserRepository @Inject()(protected val dbConfigProvider : DatabaseConfigProvider,
                               tableMappingsDAO: TableMappingsDAO) (implicit ec: ExecutionContext) extends TUserRepository with HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  private val users = tableMappingsDAO.users

  override def findById(userId: Long): Future[Seq[User]] = {
    val query = users.filter(_.userId === userId)
    val result: Future[Seq[User]] = db.run(query.result)
    result
  }

  override def save(user: User): Future[Boolean] = {
    val addAction = users += user
    db.run(addAction).map(_ => true).recover( {case _ => false})
  }

  override def delete(user: User): Future[Boolean] = {
    val deleteAction = users.filter(_.userId === user.userId).delete
    db.run(deleteAction).map(_>0)
  }

  override def update(user: User): Future[Boolean] = {
    val updateAction = users.filter(_.userId === user.userId).update(user)
    db.run(updateAction).map(_>0)
  }

  def updateProfilePicture(userId: Long, picturePath: String): Future[Boolean] = {
    val updateAction = users.filter(_.userId === userId).map(_.profilePicture).update(Some(picturePath))
    db.run(updateAction).map(_ > 0)
  }

  override def getAll : Future[Seq[User]] = {
    val query = users.result
    db.run(query)
  }

  override def getByUsername(username: String): Future[Option[User]] = {
    val query = users.filter(_.username === username)
    val result : Future[Option[User]] = db.run(query.result.headOption)
    result
  }

  override def checkPassword(user: User, password: String): Future[Boolean] = {
    val query = users.filter(_.userId === user.userId).result.headOption
    db.run(query).map {
      case Some(foundUser) if foundUser.password == password => true
      case _ => false
    }
  }

  override def getUserByUserId(userId: Long): Future[Option[User]] = {
    val query = users.filter(_.userId === userId).result.headOption
    db.run(query)
  }

  override def searchForUser(character: String): Future[Seq[User]] = {
    val searchPattern = s"%${character.toLowerCase}%"
    val query = users.filter(user =>
      (user.username.toLowerCase like searchPattern) ||
        (user.surname.toLowerCase like searchPattern) ||
        (user.name.toLowerCase like searchPattern)
    ).result
    val result: Future[Seq[User]] = db.run(query)
    result
  }

  def getUsernameByUserId(userId: Long): Future[Option[String]] = db.run {
    users.filter(_.userId === userId).map(_.username).result.headOption
  }
}
