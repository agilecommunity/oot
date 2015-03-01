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

public class DailyOrderAggregatesTest {

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order_item.yml"));
    }

    @Test
    public void showByOrderDateは指定した日付の注文状況を返すこと_1件() {

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/daily-order-aggregates/order-date/2014-02-10T00:00:00.000+09:00"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(1);
        assertThat(ParameterConverter.convertTimestampFrom(node.get(0).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+0900"));
        assertThat(node.get(0).get("code").asText()).isEqualTo("銀座肉屋　カルビハンバーグ弁当　580円");
        assertThat(node.get(0).get("numOrders").asInt()).isEqualTo(1);
    }

    @Test
    public void showByOrderDateは指定した日付の注文状況を返すこと_2件() {

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/daily-order-aggregates/order-date/2014-02-11T00:00:00.000+09:00"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(2);

        assertThat(ParameterConverter.convertTimestampFrom(node.get(0).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-11T00:00:00.000+0900"));
        assertThat(node.get(0).get("code").asText()).isEqualTo("有楽町八百屋　八品目のサラダ　230円");
        assertThat(node.get(0).get("numOrders").asInt()).isEqualTo(1);

        assertThat(ParameterConverter.convertTimestampFrom(node.get(1).get("orderDate").asText())).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-11T00:00:00.000+0900"));
        assertThat(node.get(1).get("code").asText()).isEqualTo("銀座魚屋　たっぷりサーモン丼　550円 - 20円 = 530円");
        assertThat(node.get(1).get("numOrders").asInt()).isEqualTo(5);
    }

    private Result callAPI(FakeRequest baseRequest) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie("melissa@foo.bar");
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }

}
