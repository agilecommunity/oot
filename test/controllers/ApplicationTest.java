package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import utils.Utils;
import utils.snakeyaml.YamlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static play.test.Helpers.*;

public class ApplicationTest {

    Logger.ALogger logger = Logger.of("application.controllers.DailyMenusTest");

    @Test
    public void appMetadataはアプリケーションの情報を返すこと() throws Throwable {

        String expectedVersion = "v0.0.1-17-g09cc140";
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("version", expectedVersion);
        Utils.createAppMetadata(data);

        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/app-metadata"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata = mapper.readTree(jsonString);

        assertThat(metadata.get("version").asText()).isEqualTo(expectedVersion);

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
