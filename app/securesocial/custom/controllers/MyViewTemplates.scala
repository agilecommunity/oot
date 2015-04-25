// https://typesafe.com/activator/template/play-angularjs-webapp-seed から利用
package securesocial.custom.controllers

import play.api.data.Form
import play.api.i18n.Lang
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import securesocial.controllers.{ChangeInfo, RegistrationInfo, ViewTemplates}
import securesocial.core.RuntimeEnvironment

class MyViewTemplates(env: RuntimeEnvironment[_]) extends ViewTemplates {
  implicit val implicitEnv = env

  def toHtml(jsValue: JsValue): Html = {
    Html(Json.stringify(jsValue))
  }

  def processForm[T](form: Form[T]): Html = {
    if (form.hasErrors) Html(form.errorsAsJson.toString())
    else Html("")
  }

  override def getLoginPage(form: Form[(String, String)], msg: Option[String])(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getPasswordChangePage(form: Form[ChangeInfo])(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getSignUpPage(form: Form[RegistrationInfo], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getNotAuthorizedPage(implicit request: RequestHeader, lang: Lang): Html = ???

  override def getStartSignUpPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getResetPasswordPage(form: Form[(String, String)], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }

  override def getStartResetPasswordPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
    processForm(form)
  }
}