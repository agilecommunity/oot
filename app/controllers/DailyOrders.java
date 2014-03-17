package controllers;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.DailyOrder;
import models.DailyOrderItem;
import models.LocalUser;
import models.MenuItem;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;

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
        JsonNode root_node = json.get(0);

        String user_id = root_node.findPath("user_id").asText();

        if (user_id.isEmpty()) {
            logger.debug("create user_id is empty");
            return badRequest();
        }

        String order_dateStr = root_node.findPath("order_date").asText();

        if (order_dateStr.isEmpty()) {
            logger.debug("create order_date is empty");
            return badRequest();
        }

        Date order_date = new Date(Long.parseLong(order_dateStr));

        logger.debug(String.format("create user_id:%s order_date:%s", user_id, order_date));

        DailyOrder order = DailyOrder.find_by(order_date, user_id);

        if (order != null) {
            logger.debug("create order found");
            return ok(Json.toJson(order));
        }

        order = new DailyOrder();
        order.local_user = LocalUser.find.byId(user_id);
        order.order_date = order_date;

        JsonNode detail_items = root_node.path("detail_items");

        Iterator<JsonNode> ite = detail_items.elements();

        while (ite.hasNext()) {
            JsonNode detail_item = ite.next();

            JsonNode menu_item = detail_item.path("menu_item");

            if (menu_item == null) {
                logger.debug("create menu_item not found. skip node");
                continue;
            }

            Long menu_item_id = menu_item.path("id").asLong();

            logger.debug(String.format("create menu_item_id: %d", menu_item_id));

            DailyOrderItem order_item = new DailyOrderItem();
            order_item.menu_item = MenuItem.find.byId(menu_item_id);

            order.detail_items.add(order_item);
        }


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
