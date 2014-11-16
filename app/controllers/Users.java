package controllers;

import java.util.List;

import models.LocalUser;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import filters.RequireCSRFCheck4Ng;

public class Users extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.Users");

    @RequireCSRFCheck4Ng()
    @SecureSocial.UserAwareAction
    public static Result index() {

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.identityId().userId());

        if (!localUser.isAdmin) {
            return unauthorized();
        }

        return ok(Json.toJson(LocalUser.find.all()));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.UserAwareAction
    public static Result showMe() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        logger.debug(String.format("Cookie ID: %s", request().cookie("id")));
        logger.debug(String.format("Cookie XSRF-TOKEN: %s", request().cookie("XSRF-TOKEN")));

        if (user == null) {
            logger.warn("current user not found");
            return unauthorized();
        }

        logger.debug(String.format("showMe user.email:%s", user.email().get()));

        List<LocalUser> localUsers = LocalUser.find.where().eq("email", user.email().get()).findList();

        if (localUsers.size() != 1) {
            logger.warn(String.format("showMe LocalUser not found or too many email:%s", user.email()));
            return unauthorized();
        }

        return ok(Json.toJson(localUsers.get(0)));
    }

}
