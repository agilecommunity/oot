package securesocial.custom.services

import play.api.Play
import play.api.Play.current
import play.api.mvc.{Call, RequestHeader}
import securesocial.core.IdentityProvider
import securesocial.core.services.RoutesService

class MyRoutesService extends RoutesService.Default {

  protected def baseUrl()(implicit req: RequestHeader) : String = {
    if (!Play.isTest) {
      absoluteUrl(controllers.routes.Application.index(""))
    } else {
      "http://localhost:3333/"
    }
  }

  override def loginPageUrl(implicit req: RequestHeader): String = {
    baseUrl()
  }

  override def handleSignUpUrl(mailToken: String)(implicit req: RequestHeader): String = ???

  override def startSignUpUrl(implicit req: RequestHeader): String = {
    baseUrl() + "#/signup"
  }

  override def handleStartSignUpUrl(implicit req: RequestHeader): String = ???

  override def signUpUrl(mailToken: String)(implicit req: RequestHeader): String = {
    baseUrl() + "#/signup/" + mailToken
  }

  override def passwordChangeUrl(implicit req: RequestHeader): String = ???

  override def handlePasswordChangeUrl(implicit req: RequestHeader): String = ???

  override def startResetPasswordUrl(implicit req: RequestHeader): String = {
    baseUrl() + "#/reset"
  }

  override def handleStartResetPasswordUrl(implicit req: RequestHeader): String = ???

  override def resetPasswordUrl(mailToken: String)(implicit req: RequestHeader): String = {
    baseUrl() + "#/reset/" + mailToken
  }

  override def handleResetPasswordUrl(mailToken: String)(implicit req: RequestHeader): String = ???

  override def authenticationUrl(provider: String, redirectTo: Option[String])(implicit req: RequestHeader): String = {
    super.authenticationUrl(provider, redirectTo)
  }
}
