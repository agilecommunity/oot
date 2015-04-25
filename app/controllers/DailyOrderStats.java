package controllers;

import com.avaje.ebean.*;
import models.DailyOrderStat;
import models.DailyOrderStatForDB;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.java.SecuredAction;
import utils.controller.Results;
import utils.controller.parameters.DateParameter;
import utils.controller.parameters.StatusParameter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DailyOrderStats extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyOrderStats");

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

    @SecuredAction
    public static Result index() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Parameters parameters = null;
        try {
            parameters = new Parameters(request());
        } catch (ParseException e) {
            logger.error("#index failed to parse parameters", e);
            return Results.faildToParseQueryStringError();
        }

        Query<DailyOrderStatForDB> query = Ebean.find(DailyOrderStatForDB.class);
        ExpressionList<DailyOrderStatForDB> dbObjects = query.setRawSql(createRawSql()).where();

        dbObjects = DailyOrderStats.addConditions(dbObjects, parameters);

        dbObjects.order().asc("orderDate");

        List<DailyOrderStat> objects = new ArrayList<DailyOrderStat>();
        for (DailyOrderStatForDB dbObject : dbObjects.findList()) {
            objects.add(dbObject.createOrderStat());
        }
        
        logger.debug("#index count: {}", objects.size());
        logger.debug("#index json: {}", Json.toJson(objects));

        return ok(Json.toJson(objects));
    }

    private static RawSql createRawSql() {

        String statBaseSql = "select dor.order_date as orderDate"
                + ", count(distinct dor.user_id) as numUsers"
                + ", sum(doi.num_orders) as numOrders"
                + ", sum(mi.fixed_on_order*doi.num_orders) as totalFixedOnOrder"
                + ", sum(mi.discount_on_order*doi.num_orders) as totalDiscountOnOrder"
                + " from daily_order_item doi"
                + " join menu_item mi on doi.menu_item_id = mi.id"
                + " join daily_order dor on doi.daily_order_id = dor.id";

        String totalSql = statBaseSql
                + " group by dor.order_date";

        String bentoSql = statBaseSql
                + " where mi.category = 'bento'"
                + " group by dor.order_date";

        String sideSql = statBaseSql
                + " where mi.category = 'side'"
                + " group by dor.order_date";


        String sql = ""
                + " select"
                +    " dm.menu_date, dm.status,"
                +    " all_stat.numUsers, all_stat.numOrders, all_stat.totalFixedOnOrder, all_stat.totalDiscountOnOrder,"
                +    " bento_stat.numUsers, bento_stat.numOrders, bento_stat.totalFixedOnOrder, bento_stat.totalDiscountOnOrder,"
                +    " side_stat.numUsers, side_stat.numOrders, side_stat.totalFixedOnOrder, side_stat.totalDiscountOnOrder"
                + " from daily_menu dm"
                + " left join"
                +    "(" + totalSql + ") all_stat"
                +    " on dm.menu_date = all_stat.orderDate"
                + " left join"
                +    "(" + bentoSql + ") bento_stat"
                +    " on dm.menu_date = bento_stat.orderDate"
                + " left join"
                +    "(" + sideSql + ") side_stat"
                +    " on dm.menu_date = side_stat.orderDate"
                + " where"
                + " all_stat.numUsers is not null";

        logger.trace("#createRawSql sql: {}", sql);

        RawSql rawSql = RawSqlBuilder.parse(sql)
                .columnMapping("dm.menu_date", "orderDate")
                .columnMapping("dm.status", "menuStatus")
                .columnMapping("all_stat.numUsers", "allNumUsers")
                .columnMapping("all_stat.numOrders", "allNumOrders")
                .columnMapping("all_stat.totalFixedOnOrder", "allTotalFixedOnOrder")
                .columnMapping("all_stat.totalDiscountOnOrder", "allTotalDiscountOnOrder")
                .columnMapping("bento_stat.numUsers", "bentoNumUsers")
                .columnMapping("bento_stat.numOrders", "bentoNumOrders")
                .columnMapping("bento_stat.totalFixedOnOrder", "bentoTotalFixedOnOrder")
                .columnMapping("bento_stat.totalDiscountOnOrder", "bentoTotalDiscountOnOrder")
                .columnMapping("side_stat.numUsers", "sideNumUsers")
                .columnMapping("side_stat.numOrders", "sideNumOrders")
                .columnMapping("side_stat.totalFixedOnOrder", "sideTotalFixedOnOrder")
                .columnMapping("side_stat.totalDiscountOnOrder", "sideTotalDiscountOnOrder")
                .create();

        logger.trace("#createRawSql rawSql: {}", rawSql.getSql().toString());

        return rawSql;
    }

    private static ExpressionList<DailyOrderStatForDB> addConditions(ExpressionList<DailyOrderStatForDB> base, Parameters parameters) {
        if (parameters.orderDate != null) {
            if (parameters.orderDate.isRange()) {
                DateParameter.DateRange dateRange = parameters.orderDate.getRangeValue();
                base.between("orderDate", dateRange.fromDate, dateRange.toDate);

                logger.debug("#addConditions orderDate(range) from: {}", dateRange.fromDate.toString());
                logger.debug("#addConditions orderDate(range) to  : {}", dateRange.toDate.toString());

            } else {
                base.eq("orderDate", parameters.orderDate.getValue());

                logger.debug("#addConditions orderDate(value): {}", parameters.orderDate.getValue().toString());
            }
        }

        if (parameters.status != null) {
            base.eq("menuStatus", parameters.status.getValue());

            logger.debug("#addConditions status: {}", parameters.status.getValue());
        }

        return base;
    }
}
