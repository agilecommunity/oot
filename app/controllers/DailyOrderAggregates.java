package controllers;

import com.avaje.ebean.*;
import models.DailyOrderAggregate;
import org.joda.time.DateTime;
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
    public static Result showByOrderDate(String orderDateStr) {

        logger.debug("#showByOrderDate orderDateStr: " + orderDateStr);

        String sql = "select dor.order_date, doi.menu_item_id, mi.code, sum(doi.num_orders)"
                + " from daily_order_item doi"
                + " join menu_item mi on doi.menu_item_id = mi.id"
                + " join daily_order dor on doi.daily_order_id = dor.id"
                + " group by dor.order_date, doi.menu_item_id, mi.code";

        RawSql rawSql = RawSqlBuilder.parse(sql)
                            .columnMapping("dor.order_date", "orderDate")
                            .columnMapping("doi.menu_item_id", "menuItemId")
                            .columnMapping("mi.code", "code")
                            .columnMapping("sum(doi.num_orders)", "numOrders")
                            .create();

        logger.debug("#showByOrderDate rawSql: " + rawSql.getSql().toString());

        DateTime orderDate = ParameterConverter.convertTimestampFrom(orderDateStr);

        Query<DailyOrderAggregate> query = Ebean.find(DailyOrderAggregate.class);
        query.setRawSql(rawSql)
                .where().eq("orderDate", orderDate)
                .orderBy("code");

        return ok(Json.toJson(query.findList()));
    }
}
