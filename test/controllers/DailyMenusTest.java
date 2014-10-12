package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.DailyMenu;
import models.DailyMenuItem;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.libs.Yaml;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.controller.ParameterConverter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

public class DailyMenusTest {
    Logger.ALogger logger = Logger.of("application.controllers.DailyMenusTest");

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_menu.yml"));
        Ebean.save((List) Yaml.load("fixtures/test/daily_menu_item.yml"));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createは受け取ったJsonの内容からDailyMenuオブジェクトを作成すること() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"menu_date\":\"2014-02-12\"");
        builder.append(", \"status\":\"open\"");
        builder.append(", \"detail_items\":[{\"menu_item\":{\"id\":2}}]");
        builder.append("}");

        Result result = callAPI(fakeRequest(POST, "/api/daily-menus").withJsonBody(Json.parse(builder.toString())));

        assertThat(status(result)).isEqualTo(OK);

        DateTime dateValue = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC().parseDateTime("2014-02-12");
        DailyMenu object = DailyMenu.find_by(new java.sql.Date(dateValue.getMillis()));

        assertThat(object.detail_items.size()).isEqualTo(1);
        DailyMenuItem item = object.detail_items.get(0);
        assertThat(item.menu_item.name).isEqualTo("たっぷりサーモン丼");
    }

    @Test
    public void deleteは受け取ったIdに対応するDailyMenuオブジェクトを削除すること() {

        DailyMenu target = DailyMenu.find_by(new java.sql.Date(1391958000000L)); // 2014-02-10

        Result result = callAPI(fakeRequest(DELETE, "/api/daily-menus/" + target.id));

        assertThat(status(result)).isEqualTo(OK);

        assertThat(DailyMenu.find_by(target.menu_date)).isNull();

    }

    @Test
    public void updateは受け取ったJsonの内容からDailyMenuオブジェクトを更新すること() throws IOException {

        DateTime targetDate = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC().parseDateTime("2014-02-10");

        Result findResult = callAPI(fakeRequest(GET, "/api/daily-menus/menu_date/" + targetDate.toString("yyyy-MM-dd")));

        logger.debug(String.format("result: %s", contentAsString(findResult)));

        String jsonString = contentAsString(findResult);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);

        String newMenuItem = "{\"menu_item\":{\"id\":3}}";
        JsonNode newMenuItemObj = mapper.readTree(newMenuItem);

        ((ArrayNode)(actualObj.get("detail_items"))).remove(0);
        ((ArrayNode)(actualObj.get("detail_items"))).add(newMenuItemObj);

        Result result = callAPI(fakeRequest(PUT, "/api/daily-menus/" + actualObj.get("id"))
                        .withJsonBody(Json.parse(actualObj.toString()), PUT));

        assertThat(status(result)).isEqualTo(OK);

        logger.debug(String.format("targetDate(Date): %s", targetDate.toDate().toString()));

        DailyMenu updated = DailyMenu.find_by(new java.sql.Date(targetDate.getMillis()));
        assertThat(updated.id).isEqualTo(1);
        assertThat(updated.menu_date).isEqualTo(targetDate.toDate());
        assertThat(updated.detail_items.size()).isEqualTo(1);
        assertThat(updated.detail_items.get(0).menu_item.id).isEqualTo(3);
        assertThat(updated.detail_items.get(0).menu_item.name).isEqualTo("八品目のサラダ");
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
