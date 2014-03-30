package controllers;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import models.DailyMenu;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;
import utils.controller.ParameterConverter;
import filters.RequireCSRFCheck4Ng;

public class DailyMenus extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyMenus");

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
        List<DailyMenu> menus = DailyMenu.find.findList();
        return ok(Json.toJson(menus));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByMenuDate(String menu_date_str) {

        Date menu_date;
        try {
            menu_date = ParameterConverter.convertDateFrom(menu_date_str);
        } catch (ParseException e) {
            logger.debug(String.format("showByMenuDate parse error menu_date_str:%s", menu_date_str));
            return badRequest();
        }

        DailyMenu menu = DailyMenu.find_by(menu_date);

        if (menu == null) {
            logger.debug(String.format("showByMenuDate menu not found menu_date_str:%s", menu_date_str));
            return notFound();
        }

        return ok(Json.toJson(menu));
    }

}
