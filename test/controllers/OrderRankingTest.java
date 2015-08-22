package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
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
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;

public class OrderRankingTest {

    Logger.ALogger logger = Logger.of("application.controllers.OrderRankingTest");

    @Before
    public void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
    }

    @Test
    public void indexOfLastManthは先月の注文の多い商品を返すこと_1件() {

        Result result = callAPI(fakeRequest(GET, "/api/v1.0/order-ranking/last-month"));

        assertThat(status(result)).isEqualTo(OK);

        String jsonString = contentAsString(result);

        logger.debug(jsonString);

        JsonNode node = Json.parse(jsonString);

        logger.debug(node.asText());
        logger.debug(String.valueOf(node.has("ranking")));

        JsonNode resultsNode = node.get("results");
        assertThat(resultsNode.size()).isEqualTo(1);

        JsonNode rankNode = resultsNode.get(0);
        assertThat(rankNode.get("rank").asLong()).isEqualTo(1);
        assertThat(rankNode.get("count").asLong()).isEqualTo(3);

        JsonNode rankMenuItemNode = rankNode.get("menuItem");
        assertThat(rankMenuItemNode.get("id").asLong()).isEqualTo(1);
        assertThat(rankMenuItemNode.get("name").asText()).isEqualTo("たっぷりサーモン丼");

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
