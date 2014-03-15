package controllers;

import java.util.List;

import models.DailyMenu;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import filters.RequireCSRFCheck4Ng;

public class DailyMenus extends Controller {

    @RequireCSRFCheck4Ng()
    public static Result index() {
    List<DailyMenu> menus = DailyMenu.find.findList();
        return ok(Json.toJson(menus));
    }

}
