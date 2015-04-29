package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import models.Shop;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import securesocial.custom.MySecuredAction;

import java.util.List;

public class Shops extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyMenus");

    @MySecuredAction
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        String sql
                = " select distinct shop_name as name"
                + " from menu_item"
                + " order by shop_name";

        RawSql rawSql = RawSqlBuilder
                .parse(sql)
                .create();

        Query<Shop> query = Ebean.find(Shop.class);
        query.setRawSql(rawSql);

        List<Shop> list = query.findList();

        return ok(Json.toJson(list));
    }
}
