package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.DailyMenu;
import models.DailyMenuItem;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.Utils;
import utils.controller.parameters.ParameterConverter;
import utils.snakeyaml.YamlUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;
import static play.test.Helpers.contentAsString;

public class DailyMenusTest {
    Logger.ALogger logger = Logger.of("application.controllers.DailyMenusTest");

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_menu.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_menu_item.yml"));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createは受け取ったJsonの内容からDailyMenuオブジェクトを作成すること() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"menuDate\":\"2014-02-12T00:00:00.000+09:00\"");
        builder.append(", \"status\":\"open\"");
        builder.append(", \"detailItems\":[{\"menuItem\":{\"id\":2}}]");
        builder.append("}");

        Result result = callAPI(fakeRequest(POST, "/api/v1.0/daily-menus").withJsonBody(Json.parse(builder.toString())));

        assertThat(status(result)).isEqualTo(OK);

        DateTime dateValue = ParameterConverter.convertTimestampFrom("2014-02-12T00:00:00.000+09:00");
        DailyMenu object = DailyMenu.findBy(new DateTime(dateValue.getMillis()));

        assertThat(object.detailItems.size()).isEqualTo(1);
        DailyMenuItem item = object.detailItems.get(0);
        assertThat(item.menuItem.name).isEqualTo("たっぷりサーモン丼");
    }

    @Test
    public void createは同じ商品が登録された場合BadRequestを返すこと() throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"menuDate\":\"2014-02-12T00:00:00.000+09:00\"");
        builder.append(", \"status\":\"open\"");
        builder.append(", \"detailItems\":[{\"menuItem\":{\"id\":2}}, {\"menuItem\":{\"id\":2}}]");
        builder.append("}");

        Result result = callAPI(fakeRequest(POST, "/api/v1.0/daily-menus").withJsonBody(Json.parse(builder.toString())));

        assertThat(status(result)).isEqualTo(BAD_REQUEST);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode errorMessages = mapper.readTree(jsonString);

        assertThat(errorMessages.get("detailItems").get(0).asText()).describedAs("エラーメッセージ").isEqualTo("同じ商品は登録できません");
    }

    @Test
    public void deleteは受け取ったIdに対応するDailyMenuオブジェクトを削除すること() throws ParseException {

        DailyMenu target = DailyMenu.findBy(ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+09:00"));

        Result result = callAPI(fakeRequest(DELETE, "/api/v1.0/daily-menus/" + target.id));

        assertThat(status(result)).isEqualTo(OK);

        assertThat(DailyMenu.findBy(target.menuDate)).isNull();

    }

    @Test
    public void updateは受け取ったJsonの内容からDailyMenuオブジェクトを更新すること() throws IOException {

        DateTime targetDate = ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+09:00");

        logger.debug("targetDate: {}", targetDate);

        Result findResult = callAPI(fakeRequest(GET, "/api/v1.0/daily-menus/menu-date/" + targetDate.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));

        logger.debug(String.format("result: %s", contentAsString(findResult)));

        String jsonString = contentAsString(findResult);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);

        String newMenuItem = "{\"menuItem\":{\"id\":3}}";
        JsonNode newMenuItemObj = mapper.readTree(newMenuItem);

        ((ArrayNode)(actualObj.get("detailItems"))).remove(0);
        ((ArrayNode)(actualObj.get("detailItems"))).add(newMenuItemObj);

        Result result = callAPI(fakeRequest(PUT, "/api/v1.0/daily-menus/" + actualObj.get("id"))
                        .withJsonBody(Json.parse(actualObj.toString()), PUT));

        assertThat(status(result)).isEqualTo(OK);

        logger.debug(String.format("targetDate(Date): %s", targetDate.toString()));

        DailyMenu updated = DailyMenu.findBy(targetDate);
        assertThat(updated.id).isEqualTo(1);
        assertThat(updated.menuDate).isEqualTo(targetDate);
        assertThat(updated.detailItems.size()).isEqualTo(1);
        assertThat(updated.detailItems.get(0).menuItem.id).isEqualTo(3);
        assertThat(updated.detailItems.get(0).menuItem.name).isEqualTo("八品目のサラダ");
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
