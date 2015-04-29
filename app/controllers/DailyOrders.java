package controllers;

import java.text.ParseException;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import models.DailyMenu;
import models.DailyOrder;
import models.LocalUser;
import org.joda.time.DateTime;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.custom.MySecuredAction;
import utils.controller.Results;
import utils.controller.parameters.DateParameter;
import utils.controller.parameters.ParameterConverter;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;

import filters.RequireCSRFCheck4Ng;
import utils.controller.parameters.StatusParameter;

public class DailyOrders extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyOrders");

    private static class Parameters {
        public DateParameter orderDate = null;
        public StatusParameter status = null;

        public Parameters(Http.Request request) throws ParseException {
            if (request.getQueryString("orderDate") != null) {
                this.orderDate = new DateParameter(request.getQueryString("orderDate"));
                return;
            }
            if (request.getQueryString("from") != null || request.getQueryString("to") != null) {
                this.orderDate = new DateParameter(request.getQueryString("from"), request.getQueryString("to"));
                return;
            }
            if (request.getQueryString("status") != null) {
                this.status = new StatusParameter(request.getQueryString("status"));
            }
        }
    }

    @MySecuredAction
    public static Result index() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#index failed to parse parameters", e);
            return Results.faildToParseQueryStringError();
        }

        ExpressionList<DailyOrder> query = addConditions(DailyOrder.find.where(), parameters);
        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @MySecuredAction
    public static Result getByOrderDate(String orderDateStr) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        DateTime orderDate = ParameterConverter.convertTimestampFrom(orderDateStr);

        List<DailyOrder> list = DailyOrder.findBy(orderDate);

        if (list == null || list.size() == 0) {
            logger.debug(String.format("#getByOrderDate order not found orderDateStr:%s", orderDateStr));
            return ok("[]");
        }

        return ok(Json.toJson(list));
    }

    @MySecuredAction
    public static Result getMine() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        LocalUser currentUser = getCurrentUser();

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#getMine failed to parse parameters", e);
            return Results.faildToParseQueryStringError();
        }

        ExpressionList<DailyOrder> query = addConditions(DailyOrder.find.where().eq("user_id", currentUser.id), parameters);

        query = addConditions(query, parameters);

        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @RequireCSRFCheck4Ng()
    @MySecuredAction
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("#create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        JsonNode json = request().body().asJson();

        logger.debug("#create request-body: {}", request().body().toString());

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn("#create object has some errors. {}", filledForm.errorsAsJson().toString());
            return Results.validationError(filledForm.errorsAsJson());
        }

        DailyOrder object = filledForm.get();

        LocalUser currentUser = getCurrentUser();

        if (!DailyOrders.canEdit(object, currentUser)) {
            logger.warn("#create cant create localUser.id: {} currentUser.id: {}", object.localUser.id, currentUser.id);
            return Results.insufficientPermissionsError("Current user can't create other's daily order");
        }

        if (DailyOrder.findBy(object.orderDate, object.localUser.id) != null) {
            logger.debug("#create object already exists");
            return Results.resourceAlreadyExistsError();
        }

        // メニューが閉め切られていた場合、管理者以外は編集できない
        if (!currentUser.isAdmin) {
            if (hasMenuExpired(object.orderDate)) {
                logger.debug("#create dailyMenu has expired");
                return Results.menuHasExpiredError();
            }
        }

        logger.debug("#create order.localUser.id: {}", object.localUser.id);
        logger.debug("#create order.orderDate: {}", object.orderDate);

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @MySecuredAction
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result update(Long id) {
        logger.debug("#update");

        if (DailyOrder.find.byId(id) == null) {
            logger.debug("#update object doesnt exist");
            return Results.resourceNotFoundError();
        }

        JsonNode json = request().body().asJson();

        logger.debug("#update request-body: {}", request().body().toString());

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn("#update object has some errors. {}", filledForm.errorsAsJson().toString());
            return Results.validationError(filledForm.errorsAsJson());
        }

        DailyOrder object = filledForm.get();

        LocalUser currentUser = getCurrentUser();

        if (! DailyOrders.canEdit(object, currentUser)) {
            logger.warn("#update cant update localUser.id: {} currentUser.id: {}", object.localUser.id, currentUser.id);
            return Results.insufficientPermissionsError("Current user can't update other's daily order");
        }

        // メニューが閉め切られていた場合、管理者以外は編集できない
        if (!currentUser.isAdmin) {
            if (hasMenuExpired(object.orderDate)) {
                logger.debug("#update dailyMenu has expired");
                return Results.menuHasExpiredError();
            }
        }

        logger.debug("#update order.localUser.id: {}", object.localUser.id);
        logger.debug("#update order.orderDate: {}", object.orderDate);

        object.update();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @MySecuredAction
    public static Result delete(Long id) {

        logger.debug("#delete id: {}", id);

        response().setHeader(CACHE_CONTROL, "no-cache");

        DailyOrder object = DailyOrder.find.byId(id);

        if (object == null) {
            logger.debug("#delete object not found");
            return ok();
        }

        LocalUser currentUser = getCurrentUser();

        if (! DailyOrders.canEdit(object, currentUser)) {
            logger.warn("#delete cant delete others order localUser.id: {} currentUser.id: {}", object.localUser.id, currentUser.id);
            return Results.insufficientPermissionsError("Current user can't delete other's daily order");
        }

        // メニューが閉め切られていた場合、管理者以外は編集できない
        if (!currentUser.isAdmin) {
            if (hasMenuExpired(object.orderDate)) {
                logger.debug("#update dailyMenu has expired");
                return Results.menuHasExpiredError();
            }
        }

        object.delete();

        return ok();
    }

    private static ExpressionList<DailyOrder> addConditions(ExpressionList<DailyOrder> base, Parameters parameters) {
        if (parameters.orderDate != null) {
            if (parameters.orderDate.isRange()) {
                DateParameter.DateRange dateRange = parameters.orderDate.getRangeValue();
                base.between("orderDate", dateRange.fromDate, dateRange.toDate);

                logger.debug("#addConditions orderDate(range) from: {}", dateRange.fromDate.toString());
                logger.debug("#addConditions orderDate(range) to  : {}", dateRange.toDate.toString());

            } else {
                base.eq("orderDate", parameters.orderDate.getValue());

                logger.debug("#addConditions orderDate(value) : {}", parameters.orderDate.getValue().toString());
            }
        }

        if (parameters.status != null) {
            Query<DailyMenu> subQuery = Ebean.find(DailyMenu.class)
                    .setDistinct(true)
                    .select("menuDate")
                    .where().eq("status", parameters.status.getValue())
                    .query();
            base.in("orderDate", subQuery);

            logger.debug("#addConditions status: {}", parameters.status.getValue());
        }

        return base;
    }

    private static boolean canEdit(DailyOrder order, LocalUser user) {

        logger.debug("#canEdit user.id: {}", user.id);

        // 管理者である
        if (user.isAdmin) {
            return true;
        }

        // オブジェクトの所有者である
        if (order.localUser.id.equals(user.id)) {
            return true;
        }

        return false;
    }

    private static boolean hasMenuExpired(DateTime menuDate) {

        DailyMenu menu = DailyMenu.findBy(menuDate);

        if (menu == null) {
            logger.debug("#hasMenuExpired dailyMenu not found");
            return true;
        }

        if (! DailyMenu.StatusOpen.equals(menu.status)) {
            logger.debug("#hasMenuExpired dailyMenu has expired status: {}", menu.status);
            return true;
        }

        return false;
    }

}
