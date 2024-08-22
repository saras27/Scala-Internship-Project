package repositories

import models.User

import scala.concurrent.Future

trait TUserRepository {
      def findById (userId : Long) : Future[Seq[User]]
      def save(user : User) : Future[Boolean]
      def delete(user : User) : Future[Boolean]
      def update(user : User) : Future[Boolean]
      def getAll : Future[Seq[User]]
      def getByUsername( username : String) : Future[Option[User]]
      def checkPassword( user: User, password : String) : Future[Boolean]
      def getUserByUserId(userId: Long) : Future[Option[User]]
      def searchForUser( username : String) : Future[Seq[User]]
      def getUsernameByUserId(userId: Long): Future[Option[String]]
}
