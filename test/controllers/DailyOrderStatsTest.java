package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import play.filters.csrf.CSRF;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.Utils;
import utils.controller.parameters.ParameterConverter;
import utils.snakeyaml.YamlUtil;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

public class DailyOrderStatsTest {

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_menu.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order_item.yml"));
    }

    @Test
    public void indexは指定した日付の注文状況を返すこと_1件() {

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/daily-order-stats?from=2014-02-10T00:00:00.000%2B09:00&to=2014-02-10T00:00:00.000%2B09:00"), "steve@foo.bar");

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(1);
        assertThat(ParameterConverter.convertTimestampFrom(node.get(0).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+0900"));
        assertThat(node.get(0).get("allStat").get("numUsers").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("allStat").get("numOrders").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("allStat").get("totalFixedOnOrder").asLong()).isEqualTo(580);
        assertThat(node.get(0).get("allStat").get("totalDiscountOnOrder").asLong()).isEqualTo(0);
        assertThat(node.get(0).get("allStat").get("totalReducedOnOrder").asLong()).isEqualTo(580);

    }

    @Test
    public void indexは指定した日付の注文状況を返すこと_2件() {

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/daily-order-stats?from=2014-02-10T00:00:00.000%2B09:00&to=2014-02-11T00:00:00.000%2B09:00"), "steve@foo.bar");

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(2);

        assertThat(ParameterConverter.convertTimestampFrom(node.get(0).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+0900"));
        assertThat(node.get(0).get("allStat").get("numOrders").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("allStat").get("numUsers").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("allStat").get("totalFixedOnOrder").asLong()).isEqualTo(580);
        assertThat(node.get(0).get("allStat").get("totalDiscountOnOrder").asLong()).isEqualTo(0);
        assertThat(node.get(0).get("allStat").get("totalReducedOnOrder").asLong()).isEqualTo(580);

        assertThat(ParameterConverter.convertTimestampFrom(node.get(1).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-11T00:00:00.000+0900"));
        assertThat(node.get(1).get("allStat").get("numOrders").asInt()).isEqualTo(6);
        assertThat(node.get(1).get("allStat").get("numUsers").asInt()).isEqualTo(2);
        assertThat(node.get(1).get("allStat").get("totalFixedOnOrder").asLong()).isEqualTo(550 * 2 + 230 * 1 + 550 * 3);
        assertThat(node.get(1).get("allStat").get("totalDiscountOnOrder").asLong()).isEqualTo(20 * 2 + 0 + 20 * 3);
        assertThat(node.get(1).get("allStat").get("totalReducedOnOrder").asLong()).isEqualTo((550 * 2 + 230 * 1 + 550 * 3) - (20 * 2 + 0 + 20 * 3));
    }

    @Test
    public void indexは指定したステータスの注文状況を返すこと_受付中_1件() {

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/daily-order-stats?status=open"), "steve@foo.bar");

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(1);
        assertThat(ParameterConverter.convertTimestampFrom(node.get(0).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+0900"));
        assertThat(node.get(0).get("allStat").get("numUsers").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("allStat").get("numOrders").asInt()).isEqualTo(1);
        assertThat(node.get(0).get("allStat").get("totalFixedOnOrder").asLong()).isEqualTo(580);
        assertThat(node.get(0).get("allStat").get("totalDiscountOnOrder").asLong()).isEqualTo(0);
        assertThat(node.get(0).get("allStat").get("totalReducedOnOrder").asLong()).isEqualTo(580);

    }

    private Result callAPI(FakeRequest baseRequest, String userId) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie(userId);
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }

}
