package controllers;

import java.text.ParseException;
import java.util.List;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import models.DailyMenu;
import models.LocalUser;
import org.joda.time.DateTime;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.controller.parameters.DateParameter;
import utils.controller.parameters.ParameterConverter;
import filters.RequireCSRFCheck4Ng;

public class DailyMenus extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyMenus");

    private static class Parameters {
        public DateParameter menuDate = null;

        public Parameters(Http.Request request) throws ParseException {
            if (request.getQueryString("menuDate") != null) {
                this.menuDate = new DateParameter(request.getQueryString("menuDate"));
                return;
            }
            if (request.getQueryString("from") != null || request.getQueryString("to") != null) {
                this.menuDate = new DateParameter(request.getQueryString("from"), request.getQueryString("to"));
                return;
            }
        }
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#showMine failed to parse parameters", e);
            return internalServerError();
        }

        ExpressionList<DailyMenu> menus = DailyMenu.find.where();

        if (parameters.menuDate != null) {
            if (parameters.menuDate.isRange()) {
                DateParameter.DateRange dateRange = parameters.menuDate.getRangeValue();
                menus.between("menuDate", dateRange.fromDate, dateRange.toDate);

                logger.debug("#index menuDate(range) from : " + dateRange.fromDate.toString());
                logger.debug("#index menuDate(range) to   : " + dateRange.toDate.toString());

            } else {
                menus.eq("menuDate", parameters.menuDate.getValue());

                logger.debug("#index menuDate(value) : " + parameters.menuDate.getValue().toString());
            }
        }

        return ok(Json.toJson(menus.orderBy("menuDate").findList()));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result indexByStatus(String status) {
        response().setHeader(CACHE_CONTROL, "no-cache");

        List<DailyMenu> menus = DailyMenu.find.where().eq("status", status).findList();

        return ok(Json.toJson(menus));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByMenuDate(String menuDateStr) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        DateTime menuDate = ParameterConverter.convertTimestampFrom(menuDateStr);

        DailyMenu menu = DailyMenu.findBy(menuDate);

        if (menu == null) {
            logger.debug(String.format("#showByMenuDate menu not found menu_date_str:%s", menuDateStr));
            return ok(Json.toJson(new DailyMenu()));
        }

        return ok(Json.toJson(menu));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("#create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.isAdmin) {
            logger.warn(String.format("#create only admin can create menu. localUser.id:%s", localUser.email));
            return unauthorized();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#create request-body:%s", request().body().toString()));

        Form<DailyMenu> filledForm = Form.form(DailyMenu.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("#create object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyMenu object = filledForm.get();

        logger.debug("#create DailyMenu.menuDate: " + object.menuDate.toString());

        if (DailyMenu.findBy(object.menuDate) != null) {
            logger.warn(String.format("#create object is already exists. %s", object.menuDate.toString()));
            return badRequest();
        }

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result update(Long id) {
        logger.debug("#update id: {}", id);

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.isAdmin) {
            logger.warn(String.format("#update only admin can update menu. localUser.id:%s", localUser.email));
            return unauthorized();
        }

        if (DailyMenu.find.byId(id) == null) {
            logger.debug("#update object doesn't exist");
            return badRequest();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#update request-body:%s", request().body().toString()));

        Form<DailyMenu> filledForm = Form.form(DailyMenu.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("#update object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyMenu object = filledForm.get();

        logger.debug(String.format("#update target_date:%s", object.menuDate.toString()));

        object.update();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result delete(Long id) {

        logger.debug(String.format("#delete id: %s", id));

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.isAdmin) {
            logger.warn(String.format("#delete only admin can update menu. localUser.id:%s", localUser.email));
            return unauthorized();
        }

        DailyMenu menu = DailyMenu.find.byId(id);

        if (menu == null) {
            logger.debug("#delete object not found");
            return ok();
        }

        logger.debug(String.format("#delete id:%d date:%s status:%s", menu.id, menu.menuDate.toString(), menu.status ));

        menu.delete();

        return ok();
    }

}
