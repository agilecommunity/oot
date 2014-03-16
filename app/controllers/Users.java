package controllers;

import java.util.List;

import models.LocalUser;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

public class Users extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.Users");

    //@RequireCSRFCheck4Ng()
    @SecureSocial.UserAwareAction
    public static Result showMe() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        logger.debug(String.format("showMe user.email:%s", user.email().get()));

        List<LocalUser> local_users = LocalUser.find.where().eq("email", user.email().get()).findList();

        if (local_users.size() != 1) {
            logger.warn(String.format("showMe LocalUser not found or too many email:%s", user.email()));
            return unauthorized();
        }

        return ok(Json.toJson(local_users.get(0)));
    }

}
