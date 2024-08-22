package auth

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.Configuration

import java.time.Instant
import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class AuthService @Inject()(configuration: Configuration){

  private val secretKey = configuration.get[String]("secretKey")

  def encode(userId: Long): String = {
    val claim = JwtClaim(subject = Some(userId.toString))
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }

  private def decode(token: String): Try[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS256))
  }

  def validate(token : String): Try[JwtClaim] = {
    decode(token) match {
      case Success(claim) => Success(claim)
      case Failure(exception) => Failure(exception)
    }
  }
}
