package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.MenuItem;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.Utils;
import utils.snakeyaml.YamlUtil;

import java.math.BigDecimal;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class MenuItemsTest {

    Logger.ALogger logger = Logger.of("application.controllers.MenuItemsTest");

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
    public void indexは登録されているMenuItem全てを返すこと() throws Throwable {
        Result result = callAPI(fakeRequest(GET, "/api/v1.0/menu-items"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actual = mapper.readTree(jsonString);

        assertThat(actual.size()).isEqualTo(4);

        JsonNode actualOne = actual.get(1);
        assertThat(actualOne.get("id").asLong()).isEqualTo(2L);
        assertThat(actualOne.get("name").asText()).isEqualTo("たっぷりサーモン丼");
        assertThat(actualOne.get("reducedOnOrder").asDouble()).isEqualTo(530.0);

        actualOne = actual.get(3);
        assertThat(actualOne.get("id").asLong()).isEqualTo(4L);
        assertThat(actualOne.get("name").asText()).isEqualTo("カットフルーツ盛り合わせ");
        assertThat(actualOne.get("reducedOnOrder").asDouble()).isEqualTo(280.0);

    }

    @Test
    public void createは受け取ったJsonの内容からMenuItemオブジェクトを作成すること() throws Throwable {

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        {
            // フル項目指定
            builder.append("{ \"category\": \"bento\"");
            builder.append(", \"shopName\": \"MOTTOMOTTO\"");
            builder.append(", \"registerNumber\": \"21\"");
            builder.append(", \"itemNumber\": \"①\"");
            builder.append(", \"name\": \"からあげ弁当\"");
            builder.append(", \"fixedOnOrder\":105");
            builder.append(", \"discountOnOrder\":5");
            builder.append(", \"purchasePriceExcTax\":100");
            builder.append(", \"purchasePriceIncTax\":108");
            builder.append(", \"code\": \"MOTTOMOTTO-B01\"");
            builder.append(", \"status\": \"valid\"");
            builder.append("}");
        }
        {
            builder.append(",");
            builder.append("{ \"category\": \"side\"");
            builder.append(", \"shopName\": \"ポカポカ弁当\"");
            builder.append(", \"name\": \"108種類のサラダ\"");
            builder.append(", \"fixedOnOrder\":106");
            builder.append(", \"discountOnOrder\":null");
            builder.append(", \"code\": null");
            builder.append(", \"status\": \"valid\"");
            builder.append("}");
        }
        builder.append("]");

        JsValue json = Json.parse(builder.toString());

        Result result = callAPI(fakeRequest(POST, "/api/v1.0/menu-items")
                .withJsonBody(json, POST));

        Assertions.assertThat(status(result)).isEqualTo(OK);

        List<MenuItem> items = MenuItem.find.where().or(
                com.avaje.ebean.Expr.eq("shopName", "ポカポカ弁当"),
                com.avaje.ebean.Expr.eq("shopName", "MOTTOMOTTO")
        ).order("fixedOnOrder").findList();

        MenuItem item;

        item = items.get(0);
        Assertions.assertThat(item.id).describedAs("id").isNotNull();
        Assertions.assertThat(item.category).describedAs("category").isEqualTo("bento");
        Assertions.assertThat(item.shopName).describedAs("shopName").isEqualTo("MOTTOMOTTO");
        Assertions.assertThat(item.registerNumber).describedAs("registerNumber").isEqualTo("21");
        Assertions.assertThat(item.itemNumber).describedAs("itemNumber").isEqualTo("①");
        Assertions.assertThat(item.name).describedAs("name").isEqualTo("からあげ弁当");
        Assertions.assertThat(item.fixedOnOrder).describedAs("fixedOnOrder").isEqualTo(BigDecimal.valueOf(105L));
        Assertions.assertThat(item.discountOnOrder).describedAs("discountOnOrder").isEqualTo(BigDecimal.valueOf(5L));
        Assertions.assertThat(item.purchasePriceExcTax).describedAs("purchasePriceExcTax").isEqualTo(BigDecimal.valueOf(100L));
        Assertions.assertThat(item.purchasePriceIncTax).describedAs("purchasePriceIncTax").isEqualTo(BigDecimal.valueOf(108L));
        Assertions.assertThat(item.code).describedAs("code").isEqualTo("MOTTOMOTTO-B01");
        Assertions.assertThat(item.status).describedAs("status").isEqualTo("valid");

        item = items.get(1);

        Assertions.assertThat(item.id).describedAs("id").isNotNull();
        Assertions.assertThat(item.category).describedAs("category").isEqualTo("side");
        Assertions.assertThat(item.shopName).describedAs("shopName").isEqualTo("ポカポカ弁当");
        Assertions.assertThat(item.registerNumber).describedAs("registerNumber").isEqualTo("");
        Assertions.assertThat(item.itemNumber).describedAs("itemNumber").isEqualTo("");
        Assertions.assertThat(item.name).describedAs("name").isEqualTo("108種類のサラダ");
        Assertions.assertThat(item.fixedOnOrder).describedAs("fixedOnOrder").isEqualTo(BigDecimal.valueOf(106L));
        Assertions.assertThat(item.discountOnOrder).describedAs("discountOnOrder").isEqualTo(BigDecimal.valueOf(0L));
        Assertions.assertThat(item.purchasePriceExcTax).describedAs("purchasePriceExcTax").isEqualTo(BigDecimal.valueOf(0L));
        Assertions.assertThat(item.purchasePriceIncTax).describedAs("purchasePriceIncTax").isEqualTo(BigDecimal.valueOf(0L));
        Assertions.assertThat(item.code).describedAs("code").isNull();
        Assertions.assertThat(item.status).describedAs("status").isEqualTo("valid");

    }

    @Test
    public void createは受け取ったCSVの内容からMenuItemオブジェクトを作成すること() throws Throwable {

        StringBuilder builder = new StringBuilder();
        {
            builder.append("category");
            builder.append(",shopName");
            builder.append(",registerNumber");
            builder.append(",itemNumber");
            builder.append(",name");
            builder.append(",fixedOnOrder");
            builder.append(",discountOnOrder");
            builder.append(",purchasePriceExcTax");
            builder.append(",purchasePriceIncTax");
            builder.append(",code");
            builder.append(",status");
            builder.append("\r\n");
        }
        {
            builder.append("\"bento\"");
            builder.append(",\"MOTTOMOTTO\"");
            builder.append(",\"21\"");
            builder.append(",\"①\"");
            builder.append(",\"からあげ弁当\"");
            builder.append(",105");
            builder.append(",5");
            builder.append(",100");
            builder.append(",108");
            builder.append(",\"MOTTOMOTTO-B01\"");
            builder.append(",\"valid\"");
            builder.append("\r\n");
        }
        {
            builder.append("\"side\"");
            builder.append(",\"ポカポカ弁当\"");
            builder.append(",");
            builder.append(",");
            builder.append(",\"108種類のサラダ\"");
            builder.append(",106");
            builder.append(",0");
            builder.append(",");
            builder.append(",");
            builder.append(",");
            builder.append(",\"valid\"");
        }

        Result result = callAPI(fakeRequest(POST, "/api/v1.0/menu-items")
                .withTextBody(builder.toString())
                .withHeader("Content-Type", "text/csv"));

        Assertions.assertThat(status(result)).isEqualTo(OK);

        List<MenuItem> items = MenuItem.find.where().or(
                com.avaje.ebean.Expr.eq("shopName", "ポカポカ弁当"),
                com.avaje.ebean.Expr.eq("shopName", "MOTTOMOTTO")
        ).order("fixedOnOrder").findList();

        Assertions.assertThat(items.size()).isEqualTo(2);

        MenuItem item;

        item = items.get(0);
        Assertions.assertThat(item.id).isNotNull();
        Assertions.assertThat(item.id).describedAs("id").isNotNull();
        Assertions.assertThat(item.category).describedAs("category").isEqualTo("bento");
        Assertions.assertThat(item.shopName).describedAs("shopName").isEqualTo("MOTTOMOTTO");
        Assertions.assertThat(item.registerNumber).describedAs("registerNumber").isEqualTo("21");
        Assertions.assertThat(item.itemNumber).describedAs("itemNumber").isEqualTo("①");
        Assertions.assertThat(item.name).describedAs("name").isEqualTo("からあげ弁当");
        Assertions.assertThat(item.fixedOnOrder).describedAs("fixedOnOrder").isEqualTo(BigDecimal.valueOf(105L));
        Assertions.assertThat(item.discountOnOrder).describedAs("discountOnOrder").isEqualTo(BigDecimal.valueOf(5L));
        Assertions.assertThat(item.purchasePriceExcTax).describedAs("purchasePriceExcTax").isEqualTo(BigDecimal.valueOf(100L));
        Assertions.assertThat(item.purchasePriceIncTax).describedAs("purchasePriceIncTax").isEqualTo(BigDecimal.valueOf(108L));
        Assertions.assertThat(item.code).describedAs("code").isEqualTo("MOTTOMOTTO-B01");
        Assertions.assertThat(item.status).describedAs("status").isEqualTo("valid");

        item = items.get(1);
        Assertions.assertThat(item.id).isNotNull();
        Assertions.assertThat(item.category).describedAs("category").isEqualTo("side");
        Assertions.assertThat(item.shopName).describedAs("shopName").isEqualTo("ポカポカ弁当");
        Assertions.assertThat(item.registerNumber).describedAs("registerNumber").isEqualTo("");
        Assertions.assertThat(item.itemNumber).describedAs("itemNumber").isEqualTo("");
        Assertions.assertThat(item.name).describedAs("name").isEqualTo("108種類のサラダ");
        Assertions.assertThat(item.fixedOnOrder).describedAs("fixedOnOrder").isEqualTo(BigDecimal.valueOf(106L));
        Assertions.assertThat(item.discountOnOrder).describedAs("discountOnOrder").isEqualTo(BigDecimal.valueOf(0L));
        Assertions.assertThat(item.purchasePriceExcTax).describedAs("purchasePriceExcTax").isEqualTo(BigDecimal.valueOf(0L));
        Assertions.assertThat(item.purchasePriceIncTax).describedAs("purchasePriceIncTax").isEqualTo(BigDecimal.valueOf(0L));
        Assertions.assertThat(item.code).describedAs("code").isEqualTo("");
        Assertions.assertThat(item.status).describedAs("status").isEqualTo("valid");
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
