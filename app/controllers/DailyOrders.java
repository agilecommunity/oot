package controllers;

import java.util.List;

import models.DailyOrder;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
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
        ExpressionList<DailyOrder> query = DailyOrder.find.where();

        if (request().queryString().containsKey("order_date")) {
            query.eq("order_date", request().queryString().get("order_date"));
        }

        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("create");

        JsonNode json = request().body().asJson();

        Gson gson = utils.gson.OotGsonGenerator.create();

        DailyOrder order = gson.fromJson(json.get(0).toString(), DailyOrder.class);

        if (!order.is_valid()) {
            logger.debug("create object has some errors.");
            return badRequest();
        }

        logger.debug(String.format("create order.local_user.id:%s", order.local_user.id));
        logger.debug(String.format("create order.order_date:%s", order.order_date));

        order.save();

        return ok(Json.toJson(order));
    }

}
