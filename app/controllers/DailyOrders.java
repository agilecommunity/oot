package controllers;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import models.DailyMenu;
import models.DailyOrder;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.controller.ParameterConverter;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

import filters.RequireCSRFCheck4Ng;

public class DailyOrders extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyOrders");

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showMine() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        ExpressionList<DailyOrder> query = DailyOrder.find.where().eq("user_id", user.identityId().userId());


        if (request().queryString().containsKey("order_date")) {
            query.eq("order_date", request().queryString().get("order_date"));
        }

        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByOrderDate(String order_date_str) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        java.sql.Date order_date;
        try {
            order_date = ParameterConverter.convertDateFrom(order_date_str);
        } catch (ParseException e) {
            logger.debug(String.format("showByOrderDate parse error order_date_str: %s", order_date_str));
            return badRequest();
        }

        List<DailyOrder> list = DailyOrder.find_by(order_date);

        if (list == null || list.size() == 0) {
            logger.debug(String.format("showByOrderDate order not found order_date_str:%s", order_date_str));
            return notFound();
        }

        return ok(Json.toJson(list));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result createMine() {

        logger.debug("createMine");

        response().setHeader(CACHE_CONTROL, "no-cache");

        JsonNode json = request().body().asJson();

        logger.debug(String.format("createMine request-body:%s", request().body().toString()));

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("update object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyOrder object = filledForm.get();

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!object.local_user.id.equals(user.identityId().userId())) {
            logger.warn(String.format("create cant create others order local_user.id:%s identity.user.id:%s", object.local_user.id, user.identityId().userId() ));
            return badRequest();
        }

        if (DailyOrder.find_by(object.order_date, object.local_user.id) != null) {
            logger.debug("create object already exists");
            return badRequest();
        }

        logger.debug(String.format("create order.local_user.id:%s", object.local_user.id));
        logger.debug(String.format("create order.order_date:%s", object.order_date));

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result deleteMine(Long id) {

        logger.debug(String.format("deleteMine id: %s", id));

        response().setHeader(CACHE_CONTROL, "no-cache");

        DailyOrder order = DailyOrder.find.byId(id);

        if (order == null) {
            logger.debug("deleteMine object not found");
            return ok();
        }

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!order.local_user.id.equals(user.identityId().userId())) {
            logger.warn(String.format("deleteMine cant delete others order local_user.id:%s identity.user.id:%s", order.local_user.id, user.identityId().userId() ));
            return badRequest();
        }

        order.delete();

        return ok();
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result updateMine(Long id) {

        logger.debug(String.format("update id: %s", id));

        response().setHeader(CACHE_CONTROL, "no-cache");

        if (DailyOrder.find.byId(id) == null) {
            logger.debug("update object doesnt exist");
            return badRequest();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("update request-body:%s", request().body().toString()));

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("update object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyOrder object = filledForm.get();

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!object.local_user.id.equals(user.identityId().userId())) {
            logger.warn(String.format("update cant update others order local_user.id:%s identity.user.id:%s", object.local_user.id, user.identityId().userId() ));
            return badRequest();
        }

        logger.debug(String.format("update order.local_user.id:%s", object.local_user.id));
        logger.debug(String.format("update order.order_date:%s", object.order_date));

        object.update();

        return ok(Json.toJson(object));
    }

}
