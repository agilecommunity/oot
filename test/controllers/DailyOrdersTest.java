package controllers;

import static org.fest.assertions.api.Assertions.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import java.util.Date;
import java.util.List;

import models.DailyOrder;
import models.DailyOrderItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import play.Logger;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.libs.Yaml;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.WithApplication;

import com.avaje.ebean.Ebean;

@RunWith(JUnit4.class)
public class DailyOrdersTest extends WithApplication {

    Logger.ALogger logger = Logger.of("application.controllers.DailyOrdersTest");

     @Before
     public void setUp() {
         start(fakeApplication(inMemoryDatabase()));
     }

     @After
     public void tearDown() {
     }

     @Test
     public void createは受け取ったJsonの内容からDailyOrderオブジェクトを作成すること() {
         Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));

         StringBuilder builder = new StringBuilder();
         builder.append("[");
         builder.append("{\"user_id\": \"demo@foo.baa\"");
         builder.append(", \"order_date\":1391871600000");
         builder.append(", \"detail_items\":[{\"menu_item\":{\"id\":2}}]");
         builder.append("}");
         builder.append("]");

         JsValue json = Json.parse(builder.toString());

         Cookie fake_cookie = utils.Utils.fakeCookie("demo@foo.baa");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(POST, "/api/daily-orders")
                                 .withJsonBody(json)
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         assertThat(status(result)).isEqualTo(OK);

         DailyOrder order = DailyOrder.find_by(new Date(1391871600000L), "demo@foo.baa");
         assertThat(order.local_user.first_name).isEqualTo("アジャコ");

         assertThat(order.detail_items.size()).isEqualTo(1);
         DailyOrderItem order_item = order.detail_items.get(0);
         assertThat(order_item.menu_item.name).isEqualTo("たっぷりサーモン丼");
     }

}
