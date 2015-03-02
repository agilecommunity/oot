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
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.controller.parameters.DateParameter;
import utils.controller.parameters.ParameterConverter;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;

import filters.RequireCSRFCheck4Ng;
import utils.controller.parameters.StatusParamater;

public class DailyOrders extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyOrders");

    private static class Parameters {
        public DateParameter orderDate = null;
        public StatusParamater status = null;

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
                this.status = new StatusParamater(request.getQueryString("status"));
            }
        }
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#index failed to parse parameters", e);
            return utils.controller.Results.faildToParseQueryStringError();
        }

        ExpressionList<DailyOrder> query = addConditions(DailyOrder.find.where(), parameters);
        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByOrderDate(String orderDateStr) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        DateTime orderDate = ParameterConverter.convertTimestampFrom(orderDateStr);

        List<DailyOrder> list = DailyOrder.findBy(orderDate);

        if (list == null || list.size() == 0) {
            logger.debug(String.format("#showByOrderDate order not found orderDateStr:%s", orderDateStr));
            return ok("[]");
        }

        return ok(Json.toJson(list));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showMine() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#showMine failed to parse parameters", e);
            return utils.controller.Results.faildToParseQueryStringError();
        }

        ExpressionList<DailyOrder> query = addConditions(DailyOrder.find.where().eq("user_id", user.identityId().userId()), parameters);

        query = addConditions(query, parameters);

        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("#create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#create request-body:%s", request().body().toString()));

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("#create object has some errors. %s", filledForm.errorsAsJson().toString()));
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        DailyOrder object = filledForm.get();

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!DailyOrders.canEdit(object, user)) {
            logger.warn(String.format("#create cant create localUser.id:%s identity.user.id:%s", object.localUser.id, user.identityId().userId() ));
            return utils.controller.Results.insufficientPermissionsError("Current user can't create other's daily order");
        }

        if (DailyOrder.findBy(object.orderDate, object.localUser.id) != null) {
            logger.debug("#create object already exists");
            return utils.controller.Results.resourceAlreadyExistsError();
        }

        LocalUser currentUser = LocalUser.find.where().eq("id", user.identityId().userId()).findUnique();

        // メニューが閉め切られていた場合、管理者以外は編集できない
        if (!currentUser.isAdmin) {
            if (hasMenuExpired(object.orderDate)) {
                logger.debug("#create dailyMenu has expired");
                return utils.controller.Results.menuHasExpiredError();
            }
        }

        logger.debug(String.format("#create order.localUser.id:%s", object.localUser.id));
        logger.debug(String.format("#create order.orderDate:%s", object.orderDate));

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result update(Long id) {
        logger.debug("#update");

        if (DailyOrder.find.byId(id) == null) {
            logger.debug("#update object doesnt exist");
            return utils.controller.Results.resourceNotFoundError();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#update request-body:%s", request().body().toString()));

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("#update object has some errors. %s", filledForm.errorsAsJson().toString()));
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        DailyOrder object = filledForm.get();

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (! DailyOrders.canEdit(object, user)) {
            logger.warn(String.format("#update cant update localUser.id:%s identity.user.id:%s", object.localUser.id, user.identityId().userId()));
            return utils.controller.Results.insufficientPermissionsError("Current user can't update other's daily order");
        }

        LocalUser currentUser = LocalUser.find.where().eq("id", user.identityId().userId()).findUnique();

        // メニューが閉め切られていた場合、管理者以外は編集できない
        if (!currentUser.isAdmin) {
            if (hasMenuExpired(object.orderDate)) {
                logger.debug("#update dailyMenu has expired");
                return utils.controller.Results.menuHasExpiredError();
            }
        }

        logger.debug(String.format("#update order.localUser.id:%s", object.localUser.id));
        logger.debug(String.format("#update order.orderDate:%s", object.orderDate));

        object.update();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result delete(Long id) {

        logger.debug(String.format("#delete id: %s", id));

        response().setHeader(CACHE_CONTROL, "no-cache");

        DailyOrder object = DailyOrder.find.byId(id);

        if (object == null) {
            logger.debug("#delete object not found");
            return ok();
        }

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (! DailyOrders.canEdit(object, user)) {
            logger.warn(String.format("#delete cant delete others order localUser.id:%s identity.user.id:%s", object.localUser.id, user.identityId().userId()));
            return utils.controller.Results.insufficientPermissionsError("Current user can't delete other's daily order");
        }

        LocalUser currentUser = LocalUser.find.where().eq("id", user.identityId().userId()).findUnique();

        // メニューが閉め切られていた場合、管理者以外は編集できない
        if (!currentUser.isAdmin) {
            if (hasMenuExpired(object.orderDate)) {
                logger.debug("#update dailyMenu has expired");
                return utils.controller.Results.menuHasExpiredError();
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

                logger.debug("#addConditions orderDate(range) from : " + dateRange.fromDate.toString());
                logger.debug("#addConditions orderDate(range) to   : " + dateRange.toDate.toString());

            } else {
                base.eq("orderDate", parameters.orderDate.getValue());

                logger.debug("#addConditions orderDate(value) : " + parameters.orderDate.getValue().toString());
            }
        }

        if (parameters.status != null) {
            Query<DailyMenu> subQuery = Ebean.find(DailyMenu.class)
                    .setDistinct(true)
                    .select("menuDate")
                    .where().eq("status", parameters.status.getValue())
                    .query();
            base.in("orderDate", subQuery);

            logger.debug("#addConditions status : " + parameters.status.getValue());
        }

        return base;
    }

    private static boolean canEdit(DailyOrder order, Identity user) {

        logger.debug(String.format("#canEdit identityId: %s", user.identityId()));
        logger.debug(String.format("#canEdit identityId.userId: %s", user.identityId().userId()));

        LocalUser currentUser = LocalUser.find.where().eq("id", user.identityId().userId()).findUnique();

        // 管理者である
        if (currentUser.isAdmin) {
            return true;
        }

        // オブジェクトの所有者である
        if (order.localUser.id.equals(user.identityId().userId())) {
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
            logger.debug("#hasMenuExpired dailyMenu has expired status:{}", menu.status);
            return true;
        }

        return false;
    }

}
