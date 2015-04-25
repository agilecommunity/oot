package controllers;

import models.LocalUser;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import filters.RequireCSRFCheck4Ng;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import utils.controller.Results;

public class Users extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.Users");

    @RequireCSRFCheck4Ng
    @SecuredAction
    public static Result index() {

        LocalUser localUser = getCurrentUser();

        if (!localUser.isAdmin) {
            return Results.insufficientPermissionsError("Current user can't access user list");
        }

        return ok(Json.toJson(LocalUser.find.all()));
    }

    @RequireCSRFCheck4Ng
    @SecuredAction
    public static Result getMine() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        logger.debug("#getMine Cookie ID: {}", request().cookie("id"));
        logger.debug("#getMine Cookie XSRF-TOKEN: {}", request().cookie("XSRF-TOKEN"));

        LocalUser currentUser = getCurrentUser();

        if (currentUser == null) {
            logger.warn("currentUser not found");
            return Results.insufficientPermissionsError("Current user doesn't exist");
        }

        logger.debug(String.format("#getMine currentUser: {}", Json.toJson(currentUser).toString()));

        return ok(Json.toJson(currentUser));
    }

}
