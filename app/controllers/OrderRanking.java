package controllers;

import play.libs.Json;
import play.mvc.Result;
import securesocial.custom.MySecuredAction;

public class OrderRanking extends WithSecureSocialController {

    @MySecuredAction
    public static Result indexOfLastMonth() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        return ok("{ \"results\": [ { \"rank\": 1, \"count\": 3, \"menuItem\": { \"id\": 1, \"name\": \"たっぷりサーモン丼\" } } ] }");
    }
}
