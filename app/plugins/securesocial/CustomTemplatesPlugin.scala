// https://typesafe.com/activator/template/play-angularjs-webapp-seed から利用
package plugins.securesocial

import securesocial.controllers.DefaultTemplatesPlugin
import play.api.Application
import play.api.mvc.{RequestHeader, Request}
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.core.{Identity, SecuredRequest}
import securesocial.controllers.PasswordChange.ChangeInfo
import play.api.data._
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.templates.{Txt, Html}


class CustomTemplatesPlugin(application: Application) extends DefaultTemplatesPlugin(application) {
  implicit val changeInfoFormat = Json.format[ChangeInfo]
  implicit val registrationInfoFormat = Json.format[RegistrationInfo]

  def toHtml(jsValue: JsValue): Html = {
    Html(Json.stringify(jsValue))
  }

  def processForm[T](form: Form[T]): Html = {
    if (form.hasErrors) Html(form.errorsAsJson.toString())
    else Html("")
  }

  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], errorMsg: Option[String] = None): Html = {
    errorMsg.map( message =>
      toHtml(Json.obj(
        "message" -> Messages(message)
      ))
    ).getOrElse(
        processForm(form)
      )
  }

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    processForm(form)
  }

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    processForm(form)
  }

  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    toHtml(Json.obj(
      "email" -> form.value
    ))
  }

  override def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    processForm(form)
  }

  override def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html = {
    processForm(form)
  }

  override def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Option(Html("Go to http://" + request.host + "/#/signup/" + token)))
  }

  override def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Option(Html("Go to http://" + request.host + "/#/reset/" + token)))
  }
}