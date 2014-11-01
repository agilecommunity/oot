package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import play.filters.csrf.CSRF;
import play.libs.Json;
import play.libs.Yaml;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

public class DailyOrderAggregatesTest {

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));
    }

    @Test
    public void showByOrderDateは指定した日付の注文状況を返すこと_1件() {

        Result result = callAPI(fakeRequest(GET, "/api/order-aggregates/order_date/2014-02-10"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(1);
        assertThat(node.get(0).get("order_date").asText()).isEqualTo("2014-02-10");
        assertThat(node.get(0).get("code").asText()).isEqualTo("銀座肉屋　カルビハンバーグ弁当　580円");
        assertThat(node.get(0).get("num_orders").asInt()).isEqualTo(1);
    }

    @Test
    public void showByOrderDateは指定した日付の注文状況を返すこと_2件() {

        Result result = callAPI(fakeRequest(GET, "/api/order-aggregates/order_date/2014-02-11"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        JsonNode node = Json.parse(jsonString);

        assertThat(node.size()).isEqualTo(2);

        assertThat(node.get(0).get("order_date").asText()).isEqualTo("2014-02-11");
        assertThat(node.get(0).get("code").asText()).isEqualTo("有楽町八百屋　八品目のサラダ　230円");
        assertThat(node.get(0).get("num_orders").asInt()).isEqualTo(1);

        assertThat(node.get(1).get("order_date").asText()).isEqualTo("2014-02-11");
        assertThat(node.get(1).get("code").asText()).isEqualTo("銀座魚屋　たっぷりサーモン丼　550円 - 20円 = 530円");
        assertThat(node.get(1).get("num_orders").asInt()).isEqualTo(2);
    }

    private Result callAPI(FakeRequest baseRequest) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie("melissa@foo.baa");
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }

}
