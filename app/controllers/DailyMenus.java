package controllers;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import models.DailyMenu;
import models.DailyOrder;
import models.LocalUser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.data.Form;
import play.data.format.Formatters;
import play.db.ebean.Model;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.controller.ParameterConverter;
import filters.RequireCSRFCheck4Ng;

public class DailyMenus extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyMenus");

    private static class DateRange {
        public DateTime fromDate;
        public DateTime toDate;

        public DateRange(String fromStr, String toStr) {
            this.fromDate = DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(fromStr);
            this.toDate = DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(toStr);
        }
    }

    private static class DateParameter {
        private String bareValue = "";

        private DateTime value = null;
        private DateRange rangeValue = null;

        public DateParameter(String value) {
            if (value == null) {
                return;
            }
            this.bareValue = value;

            String[] values = this.bareValue.split("-");

            if (values.length == 1) {
                this.value = DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(values[0]);
            } else if (values.length == 2) {
                this.rangeValue = new DateRange(values[0], values[1]);
            }
        }

        public boolean isRange() {
            return (this.rangeValue != null);
        }

        public DateRange getRangeValue() {
            return this.rangeValue;
        }

        public DateTime getValue() {
            return this.value;
        }
    }

    private static class Parameters {
        public DateParameter menu_date = null;

        public Parameters(Http.Request request) {
            if (request.getQueryString("menu_date") != null) {
                this.menu_date = new DateParameter(request.getQueryString("menu_date"));
            }
        }
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        Parameters parameters = new Parameters(request());

        ExpressionList<DailyMenu> menus = DailyMenu.find.where();

        if (parameters.menu_date != null) {
            if (parameters.menu_date.isRange()) {
                DateRange dateRange = parameters.menu_date.getRangeValue();
                menus.between("menu_date", dateRange.fromDate, dateRange.toDate);

                logger.debug("menu_date(range) from : " + dateRange.fromDate.toString("yyyy/MM/dd"));
                logger.debug("menu_date(range) to   : " + dateRange.toDate.toString("yyyy/MM/dd"));

            } else {
                menus.eq("menu_date", parameters.menu_date.getValue());

                logger.debug("menu_date(value) : " + parameters.menu_date.getValue().toString("yyyy/MM/dd"));
            }
        }

        return ok(Json.toJson(menus.orderBy("menu_date").findList()));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result indexByStatus(String status) {
        response().setHeader(CACHE_CONTROL, "no-cache");

        List<DailyMenu> menus = DailyMenu.find.where().eq("status", status).findList();

        return ok(Json.toJson(menus));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByMenuDate(String menu_date_str) {

        response().setHeader(CACHE_CONTROL, "no-cache");

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

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.is_admin) {
            logger.warn(String.format("create only admin can create menu. local_user.id:%s", localUser.email));
            return badRequest();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("create request-body:%s", request().body().toString()));

        Form<DailyMenu> filledForm = Form.form(DailyMenu.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("create object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyMenu object = filledForm.get();

        logger.debug("DailyMenu.menu_date: " + object.menu_date.toString());

        if (DailyMenu.find_by(object.menu_date) != null) {
            logger.warn(String.format("create object is already exists. %s", object.menu_date.toString()));
            return badRequest();
        }

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result update(Long id) {
        logger.debug("update");

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.is_admin) {
            logger.warn(String.format("update only admin can update menu. local_user.id:%s", localUser.email));
            return badRequest();
        }

        if (DailyMenu.find.byId(id) == null) {
            logger.debug("update object doesn't exist");
            return badRequest();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("update request-body:%s", request().body().toString()));

        Formatters.register(Date.class, new Formatters.SimpleFormatter<Date>() {

            @Override
            public Date parse(String input, Locale l) throws ParseException {

                return new Date(Long.parseLong(input));
            }

            @Override
            public String print(Date input, Locale l) {
                return String.valueOf(input.getTime());
            }
        });

        Form<DailyMenu> filledForm = Form.form(DailyMenu.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("update object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyMenu object = filledForm.get();

        object.update();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result delete(Long id) {

        logger.debug(String.format("delete id: %s", id));

        response().setHeader(CACHE_CONTROL, "no-cache");

        DailyMenu menu = DailyMenu.find.byId(id);

        if (menu == null) {
            logger.debug("delete object not found");
            return ok();
        }

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.is_admin) {
            logger.warn(String.format("delete only admin can update menu. local_user.id:%s", localUser.email));
            return badRequest();
        }

        menu.delete();

        return ok();
    }

}
