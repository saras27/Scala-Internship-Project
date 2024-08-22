package services

import play.api.libs.mailer.{Email, MailerClient}

import javax.inject.Inject

class EmailService @Inject()(mailerClient: MailerClient) {

  def SendResetPassword(email : String, token : String) : Unit = {
    val resetUrl = s"http://localhost:9000/api/resetPassword/$token"
    val emailContent = s"Click the link to reset your password: $resetUrl"

    val emails = Email(
      "Reset your password",
      "Your App <no-reply@yourdomain.com>",
      Seq(email),
      bodyText = Some(emailContent)
    )
    print(emails)
    mailerClient.send(emails)
  }
}
