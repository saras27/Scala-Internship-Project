package auth

import javax.inject.Inject
import pdi.jwt._
import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import javax.inject.Inject
import pdi.jwt._
import play.api.http.HeaderNames
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
case class UserRequest[A](userId: Long,  request: Request[A]) extends WrappedRequest[A](request)


class AuthAction @Inject()(bodyParser: BodyParsers.Default, authService: AuthService)(implicit ec: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent]{

  override def parser: BodyParser[AnyContent] = bodyParser
  private val headerTokenRegex = """Bearer (.+?)""".r

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    extractBearerToken(request) match {
      case Some(token) =>
        authService.validate(token) match {
          case Success(claim) => block(UserRequest(claim.subject.get.toLong, request))
          case Failure(_) => Future.successful(Results.Unauthorized("Invalid token"))
        }
      case None =>
        Future.successful(Results.Unauthorized("No token provided"))
    }
  }

  private def extractBearerToken[A](request: Request[A]): Option[String]={
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => token
    }
  }
}
