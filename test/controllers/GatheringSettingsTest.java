package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.GatheringSetting;
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
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;

public class GatheringSettingsTest {

    Logger.ALogger logger = Logger.of("application.controllers.MenuItemsTest");

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/gathering-setting.yml"));
    }

    @Test
    public void getは登録されているデータを1件返すこと() throws Throwable {
        Result result = callAPIByAdmin(fakeRequest(GET, "/api/v1.0/settings/gathering"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actual = mapper.readTree(jsonString);

        assertThat(actual.get("id").asLong()).isEqualTo(1L);
        assertThat(actual.get("enabled").asBoolean()).isEqualTo(false);
        assertThat(actual.get("minOrders").asInt()).isEqualTo(0);
        assertThat(actual.get("discountPrice").asDouble()).isEqualTo(0.0);
    }

    @Test
    public void updateはGatheringSettingオブジェクトを更新すること() throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"id\":1");
        builder.append(", \"enabled\":true");
        builder.append(", \"minOrders\":10");
        builder.append(", \"discountPrice\":20");
        builder.append("}");

        JsValue json = Json.parse(builder.toString());

        Result result = callAPIByAdmin(fakeRequest(PUT, "/api/v1.0/settings/gathering").withJsonBody(json, PUT));

        Assertions.assertThat(status(result)).isEqualTo(OK);

        GatheringSetting object = GatheringSetting.find.findUnique();

        Assertions.assertThat(object.enabled).isEqualTo(true);
        Assertions.assertThat(object.minOrders).isEqualTo(10);
        Assertions.assertThat(object.discountPrice).isEqualTo(new BigDecimal(20L));
    }

    @Test
    public void updateはDBに登録されているデータと異なるIDで更新しようとした場合400エラーを返すこと() throws Throwable {

        StringBuilder builder = new StringBuilder();
        builder.append("{ \"id\":2");
        builder.append(", \"enabled\":true");
        builder.append(", \"minOrders\":10");
        builder.append(", \"discountPrice\":20");
        builder.append("}");

        JsValue json = Json.parse(builder.toString());

        Result result = callAPIByAdmin(fakeRequest(PUT, "/api/v1.0/settings/gathering").withJsonBody(json, PUT));

        Assertions.assertThat(status(result)).isEqualTo(BAD_REQUEST);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode results = mapper.readTree(jsonString);

        Assertions.assertThat(results.get("message").asText()).describedAs("message").isEqualTo("Id is not valid");
    }

    @Test
    public void updateは一般ユーザでアクセスした場合に401エラーを返すこと() throws Throwable {

        StringBuilder builder = new StringBuilder();
        builder.append("{ \"id\":1");
        builder.append(", \"enabled\":true");
        builder.append(", \"minOrders\":10");
        builder.append(", \"discountPrice\":20");
        builder.append("}");

        JsValue json = Json.parse(builder.toString());

        Result result = callAPIByUser(fakeRequest(PUT, "/api/v1.0/settings/gathering").withJsonBody(json, PUT));

        Assertions.assertThat(status(result)).isEqualTo(UNAUTHORIZED);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode results = mapper.readTree(jsonString);

        Assertions.assertThat(results.get("message").asText()).describedAs("message").isEqualTo("Current user can't update.");
    }

    private Result callAPIByAdmin(FakeRequest baseRequest) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie("melissa@foo.bar");
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }

    private Result callAPIByUser(FakeRequest baseRequest) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie("bob@foo.bar");
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }

}
