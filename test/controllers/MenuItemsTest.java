package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.MenuItem;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.api.libs.Files;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.api.mvc.AnyContentAsMultipartFormData;
import play.api.mvc.AnyContentAsMultipartFormData$;
import play.api.mvc.MultipartFormData;
import play.filters.csrf.CSRF;
import play.libs.Yaml;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import scala.collection.mutable.Map$;
import scala.collection.mutable.Seq;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class MenuItemsTest {

    Logger.ALogger logger = Logger.of("application.controllers.MenuItemsTest");

    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
        Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));
    }

    @Test
    public void indexは登録されているMenuItem全てを返すこと() throws Throwable {
        Result result = callAPI(fakeRequest(GET, "/api/menu-items"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actual = mapper.readTree(jsonString);

        assertThat(actual.size()).isEqualTo(4);

        JsonNode actualOne = actual.get(0);
        assertThat(actualOne.get("id").asLong()).isEqualTo(1L);
        assertThat(actualOne.get("name").asText()).isEqualTo("カルビハンバーグ弁当");

        actualOne = actual.get(3);
        assertThat(actualOne.get("id").asLong()).isEqualTo(4L);
        assertThat(actualOne.get("name").asText()).isEqualTo("カットフルーツ盛り合わせ");

    }

    @Test
    public void createは受け取ったJsonの内容からMenuItemオブジェクトを作成すること() throws Throwable {

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        {
            builder.append("{ \"category\": \"bento\"");
            builder.append(", \"shop_name\": \"MOTTOMOTTO\"");
            builder.append(", \"name\": \"からあげ弁当\"");
            builder.append(", \"price_on_order\":105");
            builder.append(", \"code\": \"MOTTOMOTTO-B01\"");
            builder.append("}");
        }
        {
            builder.append(",");
            builder.append("{ \"category\": \"side\"");
            builder.append(", \"shop_name\": \"ポカポカ弁当\"");
            builder.append(", \"name\": \"108種類のサラダ\"");
            builder.append(", \"price_on_order\":106");
            builder.append(", \"code\": null");
            builder.append("}");
        }
        builder.append("]");

        JsValue json = Json.parse(builder.toString());

        Result result = callAPI(fakeRequest(POST, "/api/menu-items")
                .withJsonBody(json, POST));

        Assertions.assertThat(status(result)).isEqualTo(OK);

        List<MenuItem> items = MenuItem.find.where().or(
                com.avaje.ebean.Expr.eq("shop_name", "ポカポカ弁当"),
                com.avaje.ebean.Expr.eq("shop_name", "MOTTOMOTTO")
        ).order("price_on_order").findList();

        Assertions.assertThat(items.size()).isEqualTo(2);

        MenuItem item;

        item = items.get(0);
        Assertions.assertThat(item.id).isNotNull();
        Assertions.assertThat(item.category).isEqualTo("bento");
        Assertions.assertThat(item.shop_name).isEqualTo("MOTTOMOTTO");
        Assertions.assertThat(item.name).isEqualTo("からあげ弁当");
        Assertions.assertThat(item.price_on_order).isEqualTo(BigDecimal.valueOf(105L));
        Assertions.assertThat(item.code).isEqualTo("MOTTOMOTTO-B01");

        item = items.get(1);
        Assertions.assertThat(item.id).isNotNull();
        Assertions.assertThat(item.category).isEqualTo("side");
        Assertions.assertThat(item.shop_name).isEqualTo("ポカポカ弁当");
        Assertions.assertThat(item.name).isEqualTo("108種類のサラダ");
        Assertions.assertThat(item.price_on_order).isEqualTo(BigDecimal.valueOf(106L));
        Assertions.assertThat(item.code).isEmpty();

    }

    @Test
    public void createは受け取ったCSVの内容からMenuItemオブジェクトを作成すること() throws Throwable {

        StringBuilder builder = new StringBuilder();
        {
            builder.append("category");
            builder.append(",shop_name");
            builder.append(",name");
            builder.append(",price_on_order");
            builder.append(",code");
            builder.append("\r\n");
        }
        {
            builder.append("\"bento\"");
            builder.append(",\"MOTTOMOTTO\"");
            builder.append(",\"からあげ弁当\"");
            builder.append(",105");
            builder.append(",\"MOTTOMOTTO-B01\"");
            builder.append("\r\n");
        }
        {
            builder.append("\"side\"");
            builder.append(",\"ポカポカ弁当\"");
            builder.append(",\"108種類のサラダ\"");
            builder.append(",106");
            builder.append(",");
        }

        Result result = callAPI(fakeRequest(POST, "/api/menu-items")
                .withTextBody(builder.toString())
                .withHeader("Content-Type", "text/csv"));

        Assertions.assertThat(status(result)).isEqualTo(OK);

        List<MenuItem> items = MenuItem.find.where().or(
                com.avaje.ebean.Expr.eq("shop_name", "ポカポカ弁当"),
                com.avaje.ebean.Expr.eq("shop_name", "MOTTOMOTTO")
        ).order("price_on_order").findList();

        Assertions.assertThat(items.size()).isEqualTo(2);

        MenuItem item;

        item = items.get(0);
        Assertions.assertThat(item.id).isNotNull();
        Assertions.assertThat(item.category).isEqualTo("bento");
        Assertions.assertThat(item.shop_name).isEqualTo("MOTTOMOTTO");
        Assertions.assertThat(item.name).isEqualTo("からあげ弁当");
        Assertions.assertThat(item.price_on_order).isEqualTo(BigDecimal.valueOf(105L));
        Assertions.assertThat(item.code).isEqualTo("MOTTOMOTTO-B01");

        item = items.get(1);
        Assertions.assertThat(item.id).isNotNull();
        Assertions.assertThat(item.category).isEqualTo("side");
        Assertions.assertThat(item.shop_name).isEqualTo("ポカポカ弁当");
        Assertions.assertThat(item.name).isEqualTo("108種類のサラダ");
        Assertions.assertThat(item.price_on_order).isEqualTo(BigDecimal.valueOf(106L));
        Assertions.assertThat(item.code).isEmpty();

    }

    @Test
    public void createは受け取ったファイルの内容からMenuItemオブジェクトを作成すること() throws Throwable {


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
