package controllers;

import com.avaje.ebean.*;
import models.DailyOrderAggregate;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.custom.MySecuredAction;
import utils.controller.Results;
import utils.controller.parameters.DateParameter;
import utils.controller.parameters.ParameterConverter;

import java.text.ParseException;

public class DailyOrderAggregates extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.OrderLists");

    private static class Parameters {
        public DateParameter orderDate = null;

        public Parameters(Http.Request request) throws ParseException {
            if (request.getQueryString("from") != null || request.getQueryString("to") != null) {
                this.orderDate = new DateParameter(request.getQueryString("from"), request.getQueryString("to"));
                return;
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

        logger.debug("#index rawSql: {}", rawSql.getSql().toString());

        Query<DailyOrderAggregate> query = Ebean.find(DailyOrderAggregate.class);
        ExpressionList<DailyOrderAggregate> aggregates = query.setRawSql(rawSql).where();

        if (parameters.orderDate != null) {
            if (parameters.orderDate.isRange()) {
                DateParameter.DateRange dateRange = parameters.orderDate.getRangeValue();
                aggregates.between("orderDate", dateRange.fromDate, dateRange.toDate);
            }
        }

        aggregates.order().asc("orderDate").order().asc("code");

        return ok(Json.toJson(aggregates.findList()));
    }

    @MySecuredAction
    public static Result getByOrderDate(String orderDateStr) {

        logger.debug("#getByOrderDate orderDateStr: " + orderDateStr);

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

        logger.debug("#getByOrderDate rawSql: {}", rawSql.getSql().toString());

        DateTime orderDate = ParameterConverter.convertTimestampFrom(orderDateStr);

        Query<DailyOrderAggregate> query = Ebean.find(DailyOrderAggregate.class);
        query.setRawSql(rawSql)
                .where().eq("orderDate", orderDate)
                .orderBy("code");

        return ok(Json.toJson(query.findList()));
    }
}
