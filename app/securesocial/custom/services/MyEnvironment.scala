package securesocial.custom.services

import models.LocalUser
import securesocial.controllers.ViewTemplates
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core.services.{RoutesService, UserService}
import securesocial.core.RuntimeEnvironment
import securesocial.custom.controllers.MyViewTemplates

import scala.collection.immutable.ListMap

class MyEnvironment extends RuntimeEnvironment.Default[LocalUser] {
  override val userService: UserService[LocalUser] = new MyUserService()
  override lazy val routes: RoutesService = new MyRoutesService()
  override lazy val viewTemplates: ViewTemplates = new MyViewTemplates(this)
  override lazy val providers = ListMap(
    // username password
    include(new UsernamePasswordProvider[LocalUser](userService, avatarService, viewTemplates, passwordHashers))
  )
}