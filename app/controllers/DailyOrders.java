package controllers;

import java.util.List;

import models.DailyOrder;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

import filters.RequireCSRFCheck4Ng;

public class DailyOrders extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyOrders");

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showMine() {
        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        ExpressionList<DailyOrder> query = DailyOrder.find.where().eq("user_id", user.identityId().userId());


        if (request().queryString().containsKey("order_date")) {
            query.eq("order_date", request().queryString().get("order_date"));
        }

        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result createMine() {

        logger.debug("create");

        JsonNode json = request().body().asJson();

        logger.debug(String.format("create request-body:%s", request().body().toString()));

        Gson gson = utils.gson.OotGsonGenerator.create();

        if (!json.has(0)) {
            logger.debug("create invalid json format");
            return badRequest();
        }

        DailyOrder order = gson.fromJson(json.get(0).toString(), DailyOrder.class);

        if (!order.is_valid()) {
            logger.debug("create object has some errors.");
            return badRequest();
        }

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!order.local_user.id.equals(user.identityId().userId())) {
            logger.warn(String.format("create cant create others order local_user.id:%s identity.user.id:%s", order.local_user.id, user.identityId().userId() ));
            return badRequest();
        }

        if (DailyOrder.find_by(order.order_date, order.local_user.id) != null) {
            logger.debug("create object already exists");
            return badRequest();
        }

        logger.debug(String.format("create order.local_user.id:%s", order.local_user.id));
        logger.debug(String.format("create order.order_date:%s", order.order_date));

        order.save();

        return ok(Json.toJson(order));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result deleteMine(Long id) {

        logger.debug(String.format("deleteMine id: %s", id));

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

        if (DailyOrder.find.byId(id) == null) {
            logger.debug("update object doesnt exist");
            return badRequest();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("update request-body:%s", request().body().toString()));

        Gson gson = utils.gson.OotGsonGenerator.create();

        DailyOrder order = gson.fromJson(json.toString(), DailyOrder.class);

        if (!order.is_valid()) {
            logger.debug("update object has some errors.");
            return badRequest();
        }

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!order.local_user.id.equals(user.identityId().userId())) {
            logger.warn(String.format("update cant update others order local_user.id:%s identity.user.id:%s", order.local_user.id, user.identityId().userId() ));
            return badRequest();
        }

        logger.debug(String.format("update order.local_user.id:%s", order.local_user.id));
        logger.debug(String.format("update order.order_date:%s", order.order_date));

        order.update();

        return ok(Json.toJson(order));
    }

}
