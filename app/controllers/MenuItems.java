package controllers;

import filters.RequireCSRFCheck4Ng;
import models.DailyMenu;
import models.MenuItem;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.util.List;

public class MenuItems extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.MenuItems");

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        List<MenuItem> menus = MenuItem.find.orderBy("id").findList();

        return ok(Json.toJson(menus));
    }
}
