package controllers;

import com.avaje.ebean.*;
import models.DailyOrderAggregate;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;
import utils.controller.ParameterConverter;

import java.text.ParseException;

public class DailyOrderAggregates extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.OrderLists");

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByOrderDate(String order_date_str) {

        logger.debug("showByOrderDate order_date_str: " + order_date_str);

        String sql = "select do.order_date, doi.menu_item_id, mi.code, count(doi.menu_item_id)"
                + " from daily_order_item doi"
                + " join menu_item mi on doi.menu_item_id = mi.id"
                + " join daily_order do on doi.daily_order_id = do.id"
                + " group by do.order_date, doi.menu_item_id";

        RawSql rawSql = RawSqlBuilder.parse(sql)
                            .columnMapping("do.order_date", "order_date")
                            .columnMapping("doi.menu_item_id", "menu_item_id")
                            .columnMapping("mi.code", "code")
                            .columnMapping("count(doi.menu_item_id)", "num_orders")
                            .create();

        logger.debug("showByOrderDate rawSql: " + rawSql.getSql().toString());

        java.sql.Date order_date;
        try {
            order_date = ParameterConverter.convertDateFrom(order_date_str);
        } catch (ParseException e) {
            logger.debug(String.format("showByOrderDate parse error order_date_str: %s", order_date_str));
            return badRequest();
        }

        Query<DailyOrderAggregate> query = Ebean.find(DailyOrderAggregate.class);
        query.setRawSql(rawSql)
                .where().eq("order_date", order_date)
                .orderBy("code");

        return ok(Json.toJson(query.findList()));
    }
}
