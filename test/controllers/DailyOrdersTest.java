package controllers;

import static org.fest.assertions.api.Assertions.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import models.DailyOrder;
import models.DailyOrderItem;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import play.Logger;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.libs.Yaml;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.WithApplication;

import com.avaje.ebean.Ebean;

@RunWith(JUnitParamsRunner.class)
public class DailyOrdersTest extends WithApplication {

    Logger.ALogger logger = Logger.of("application.controllers.DailyOrdersTest");

     @Before
     public void setUp() {
         start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
         Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));
     }

     @After
     public void tearDown() {
     }

     @Test
     public void createは受け取ったJsonの内容からDailyOrderオブジェクトを作成すること() {
         StringBuilder builder = new StringBuilder();
         builder.append("{ \"localUser\":{\"id\": \"steve@foo.bar\"}");
         builder.append(", \"orderDate\":\"2014-03-11\"");
         builder.append(", \"detailItems\":[{\"menuItem\":{\"id\":2}}]");
         builder.append("}");

         JsValue json = Json.parse(builder.toString());

         Result result = callCreate(builder.toString());

         assertThat(status(result)).isEqualTo(OK);

         DateTime dateValue = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC().parseDateTime("2014-03-11");
         DailyOrder order = DailyOrder.findBy(new java.sql.Date(dateValue.getMillis()), "steve@foo.bar");
         assertThat(order.localUser.firstName).isEqualTo("スティーブ");

         assertThat(order.detailItems.size()).isEqualTo(1);
         DailyOrderItem order_item = order.detailItems.get(0);
         assertThat(order_item.menuItem.name).isEqualTo("たっぷりサーモン丼");
     }


     @Test
     @Parameters(method = "illegal_json_data")
     public void createはJsonの内容が不正であった場合400_BadRequestを返すこと(String jsonString) {
         Result result = callCreate(jsonString);

         assertThat(status(result)).isEqualTo(BAD_REQUEST);
     }

     @Test
     public void deleteは指定したIDのDailyOrderオブジェクトを削除すること() {
         assertThat(DailyOrder.find.byId(1L)).isNotNull();

         Result result = callDelete(1L);

         assertThat(status(result)).isEqualTo(OK);

         DailyOrder order = DailyOrder.find.byId(1L);

         assertThat(order).isNull();

     }

     @Test
     public void updateは指定したIDのDailyOrderオブジェクトを更新すること() {
         StringBuilder builder = new StringBuilder();
         builder.append("{ \"id\":1");
         builder.append(", \"orderDate\":\"2014-02-10\"");
         builder.append(", \"localUser\":{\"id\": \"steve@foo.bar\"}");
         builder.append(", \"detailItems\":[{\"menuItem\":{\"id\":2}}]");
         builder.append("}");

         JsValue json = Json.parse(builder.toString());

         Result result = callUpdate(1L, builder.toString());

         assertThat(status(result)).isEqualTo(OK);

         DailyOrder order = DailyOrder.find.byId(1L);
         assertThat(order.localUser.firstName).isEqualTo("スティーブ");

         assertThat(order.detailItems.size()).isEqualTo(1);
         DailyOrderItem order_item = order.detailItems.get(0);
         assertThat(order_item.menuItem.name).isEqualTo("たっぷりサーモン丼");
     }

     private Result callCreate(String jsonString) {

         JsValue json = Json.parse(jsonString);
         Cookie fake_cookie = utils.Utils.fakeCookie("steve@foo.bar");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(POST, "/api/v1.0/daily-orders")
                                 .withJsonBody(json)
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         return result;
     }

     private Result callDelete(Long id) {

         Cookie fake_cookie = utils.Utils.fakeCookie("steve@foo.bar");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(DELETE, String.format("/api/v1.0/daily-orders/%d", id))
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         return result;
     }

     private Result callUpdate(Long id, String jsonString) {

         JsValue json = Json.parse(jsonString);
         Cookie fake_cookie = utils.Utils.fakeCookie("steve@foo.bar");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(PUT, String.format("/api/v1.0/daily-orders/%d", id))
                                 .withJsonBody(json, PUT)  // PUTがないとPOSTしてしまうらしい(バグか?)
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         return result;
     }

     private Object[] illegal_json_data() {
         return JUnitParamsRunner.$(
                   JUnitParamsRunner.$("{ }") // 空のリクエスト
                 , JUnitParamsRunner.$("{ \"localUser\": {\"id\":\"steve@foo.bar\"} }") // 必須項目(orderDate)なし
                 , JUnitParamsRunner.$("{ \"orderDate\":\"2014-02-11\" }")             // 必須項目(localUser)なし
                 , JUnitParamsRunner.$("{ \"localUser\": {\"id\":\"hoge\"}, \"orderDate\":\"2014-02-11\" }") // 存在しないユーザ
                 , JUnitParamsRunner.$("{ \"orderDate\":\"aaa\" }") // 存在しない日付
                 , JUnitParamsRunner.$("{ \"localUser\": {\"id\":\"steve@foo.bar\"}, \"orderDate\":\"2014-02-10\" }") // 登録済みの注文
                 , JUnitParamsRunner.$("{ \"localUser\": {\"id\":\"bob@foo.bar\"}, \"orderDate\":\"2014-02-10\" }") // ユーザが異なる
                 );

     }
}
