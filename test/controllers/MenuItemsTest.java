package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.filters.csrf.CSRF;
import play.libs.Yaml;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.List;

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

    private Result callAPI(FakeRequest baseRequest) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie("melissa@foo.baa");
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }
}
