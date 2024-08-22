package auth

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtOptions}
import play.api.Configuration

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class EmailAuth @Inject()(configuration: Configuration)(implicit ec : ExecutionContext){

  private val secretKey = configuration.get[String]("secretKey")

  def generateForPassword(userId : Long) : String = {
    val expirationTime = Instant.now.plusSeconds(900)
    val claim = JwtClaim(
      subject = Some(userId.toString),
      expiration = Some(expirationTime.getEpochSecond)
    )
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }

    def validatePasswordToken(token: String): Future[JwtClaim] = Future {
    Jwt.decode(token, JwtOptions(signature = false)) match {
      case Success(claim) => claim
      case Failure(exception) =>
        val x = exception.getMessage
        throw exception
    }
  }
}
