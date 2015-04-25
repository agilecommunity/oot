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
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.java.SecuredAction;
import utils.controller.Results;
import utils.controller.parameters.DateParameter;
import utils.controller.parameters.ParameterConverter;
import filters.RequireCSRFCheck4Ng;

public class DailyMenus extends WithSecureSocialController {

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

    @SecuredAction
    public static Result index() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#index failed to parse parameters", e);
            return Results.faildToParseQueryStringError();
        }

        ExpressionList<DailyMenu> menus = DailyMenu.find.where();

        if (parameters.menuDate != null) {
            if (parameters.menuDate.isRange()) {
                DateParameter.DateRange dateRange = parameters.menuDate.getRangeValue();
                menus.between("menuDate", dateRange.fromDate, dateRange.toDate);

                logger.debug("#index menuDate(range) from : {}", dateRange.fromDate.toString());
                logger.debug("#index menuDate(range) to   : {}", dateRange.toDate.toString());

            } else {
                menus.eq("menuDate", parameters.menuDate.getValue());

                logger.debug("#index menuDate(value) : {}", parameters.menuDate.getValue().toString());
            }
        }

        return ok(Json.toJson(menus.orderBy("menuDate").findList()));
    }

    @SecuredAction
    public static Result indexByStatus(String status) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        List<DailyMenu> menus = DailyMenu.find.where().eq("status", status).findList();

        return ok(Json.toJson(menus));
    }

    @SecuredAction
    public static Result getByMenuDate(String menuDateStr) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        DateTime menuDate = ParameterConverter.convertTimestampFrom(menuDateStr);

        DailyMenu menu = DailyMenu.findBy(menuDate);

        if (menu == null) {
            logger.debug("#getByMenuDate menu not found menu_date_str: {}", menuDateStr);
            return ok(Json.toJson(new DailyMenu())); // 404ではなく、空のオブジェクトを返す
        }

        return ok(Json.toJson(menu));
    }

    @RequireCSRFCheck4Ng()
    @SecuredAction
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("#create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        LocalUser currentUser = getCurrentUser();

        if (!currentUser.isAdmin) {
            logger.warn("#create only admin can create menu. localUser.id:{}", currentUser.email);
            return Results.insufficientPermissionsError("Current user can't create daily menu");
        }

        JsonNode json = request().body().asJson();

        logger.debug("#create request-body: {}", request().body().toString());

        Form<DailyMenu> filledForm = Form.form(DailyMenu.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn("#create object has some errors. {}", filledForm.errorsAsJson().toString());
            return Results.validationError(filledForm.errorsAsJson());
        }

        DailyMenu object = filledForm.get();

        logger.debug("#create DailyMenu.menuDate: {}", object.menuDate.toString());

        if (DailyMenu.findBy(object.menuDate) != null) {
            logger.warn("#create object is already exists. {}", object.menuDate.toString());
            return Results.resourceAlreadyExistsError();
        }

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecuredAction
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result update(Long id) {
        logger.debug("#update id: {}", id);

        response().setHeader(CACHE_CONTROL, "no-cache");

        LocalUser currentUser = getCurrentUser();

        if (!currentUser.isAdmin) {
            logger.warn("#update only admin can update menu. localUser.id: {}", currentUser.email);
            return Results.insufficientPermissionsError("Current user can't update daily menu");
        }

        if (DailyMenu.find.byId(id) == null) {
            logger.debug("#update object doesn't exist");
            return Results.resourceNotFoundError();
        }

        JsonNode json = request().body().asJson();

        logger.debug("#update request-body: {}", request().body().toString());

        Form<DailyMenu> filledForm = Form.form(DailyMenu.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn("#update object has some errors. {}", filledForm.errorsAsJson().toString());
            return Results.validationError(filledForm.errorsAsJson().toString());
        }

        DailyMenu object = filledForm.get();

        logger.debug("#update target_date: {}", object.menuDate.toString());

        object.update();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecuredAction
    public static Result delete(Long id) {

        logger.debug("#delete id: {}", id);

        response().setHeader(CACHE_CONTROL, "no-cache");

        LocalUser currentUser = getCurrentUser();

        if (!currentUser.isAdmin) {
            logger.warn("#delete only admin can update menu. localUser.id: {}", currentUser.email);
            return Results.insufficientPermissionsError("Current user cant delete daily menu");
        }

        DailyMenu menu = DailyMenu.find.byId(id);

        if (menu == null) {
            logger.debug("#delete object not found");
            return ok();
        }

        logger.debug("#delete id: {} date: {} status: {}", menu.id, menu.menuDate.toString(), menu.status );

        menu.delete();

        return ok();
    }

}
