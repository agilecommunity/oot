package controllers;

import java.util.List;

import models.DailyMenu;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;
import filters.RequireCSRFCheck4Ng;

public class DailyMenus extends Controller {

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
    List<DailyMenu> menus = DailyMenu.find.findList();
        return ok(Json.toJson(menus));
    }

}
