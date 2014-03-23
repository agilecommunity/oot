package controllers;

import static org.fest.assertions.api.Assertions.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;

import java.util.Date;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import models.DailyOrder;
import models.DailyOrderItem;

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
         start(fakeApplication(inMemoryDatabase()));
         Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));
     }

     @After
     public void tearDown() {
     }

     @Test
     public void createMineは受け取ったJsonの内容からDailyOrderオブジェクトを作成すること() {
         StringBuilder builder = new StringBuilder();
         builder.append("[");
         builder.append("{ \"local_user\":{\"id\": \"demo@foo.baa\"}");
         builder.append(", \"order_date\":1391871600000");
         builder.append(", \"detail_items\":[{\"menu_item\":{\"id\":2}}]");
         builder.append("}");
         builder.append("]");

         JsValue json = Json.parse(builder.toString());

         Result result = callCreateMine(builder.toString());

         assertThat(status(result)).isEqualTo(OK);

         DailyOrder order = DailyOrder.find_by(new Date(1391871600000L), "demo@foo.baa");
         assertThat(order.local_user.first_name).isEqualTo("アジャコ");

         assertThat(order.detail_items.size()).isEqualTo(1);
         DailyOrderItem order_item = order.detail_items.get(0);
         assertThat(order_item.menu_item.name).isEqualTo("たっぷりサーモン丼");
     }


     @Test
     @Parameters(method = "illegal_json_data")
     public void createMineはJsonの内容が不正であった場合400_BadRequestを返すこと(String jsonString) {
         Result result = callCreateMine(jsonString);

         assertThat(status(result)).isEqualTo(BAD_REQUEST);
     }

     @Test
     public void deleteMineは指定したIDのDailyOrderオブジェクトを削除すること() {
         assertThat(DailyOrder.find.findRowCount()).isEqualTo(1);

         Result result = callDeleteMine(1L);

         assertThat(status(result)).isEqualTo(OK);

         DailyOrder order = DailyOrder.find.byId(1L);

         assertThat(order).isNull();

     }

     @Test
     public void updateMineは指定したIDのDailyOrderオブジェクトを更新すること() {
         StringBuilder builder = new StringBuilder();
         builder.append("{ \"id\":1");
         builder.append(", \"order_date\":1391958000000");
         builder.append(", \"local_user\":{\"id\": \"demo@foo.baa\"}");
         builder.append(", \"detail_items\":[{\"menu_item\":{\"id\":2}}]");
         builder.append("}");

         JsValue json = Json.parse(builder.toString());

         Result result = callUpdateMine(1L, builder.toString());

         assertThat(status(result)).isEqualTo(OK);

         DailyOrder order = DailyOrder.find.byId(1L);
         assertThat(order.local_user.first_name).isEqualTo("アジャコ");

         assertThat(order.detail_items.size()).isEqualTo(1);
         DailyOrderItem order_item = order.detail_items.get(0);
         assertThat(order_item.menu_item.name).isEqualTo("たっぷりサーモン丼");
     }

     private Result callCreateMine(String jsonString) {

         JsValue json = Json.parse(jsonString);
         Cookie fake_cookie = utils.Utils.fakeCookie("demo@foo.baa");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(POST, "/api/daily-orders/mine")
                                 .withJsonBody(json)
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         return result;
     }

     private Result callDeleteMine(Long id) {

         Cookie fake_cookie = utils.Utils.fakeCookie("demo@foo.baa");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(DELETE, String.format("/api/daily-orders/mine/%d", id))
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         return result;
     }

     private Result callUpdateMine(Long id, String jsonString) {

         JsValue json = Json.parse(jsonString);
         Cookie fake_cookie = utils.Utils.fakeCookie("demo@foo.baa");
         String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

         Result result = route(fakeRequest(PUT, String.format("/api/daily-orders/mine/%d", id))
                                 .withJsonBody(json, PUT)  // PUTがないとPOSTしてしまうらしい(バグか?)
                                 .withCookies(fake_cookie)
                                 .withHeader("X-XSRF-TOKEN", token)
                                 .withSession("XSRF-TOKEN", token));

         return result;
     }

     private Object[] illegal_json_data() {
         return JUnitParamsRunner.$(
                   JUnitParamsRunner.$("[{ }]") // 空のリクエスト
                 , JUnitParamsRunner.$("[{ \"local_user\": {\"id\":\"demo@foo.baa\"} }]") // 必須項目(order_date)なし
                 , JUnitParamsRunner.$("[{ \"order_date\":1391871600000 }]")             // 必須項目(local_user)なし
                 , JUnitParamsRunner.$("[{ \"local_user\": {\"id\":\"hoge\"}, \"order_date\":1391871600000 }]") // 存在しないユーザ
                 , JUnitParamsRunner.$("[{ \"order_date\":\"aaa\" }]") // 存在しない日付
                 , JUnitParamsRunner.$("[{ \"local_user\": {\"id\":\"demo@foo.baa\"}, \"order_date\":1391958000000 }]") // 登録済みの注文
                 , JUnitParamsRunner.$("[{ \"local_user\": {\"id\":\"bob@foo.baa\"}, \"order_date\":1391871600000 }]") // ユーザが異なる
                 , JUnitParamsRunner.$("{ \"local_user\": {\"id\":\"demo@foo.baa\"}, \"order_date\":1391871600000 }") // 配列でない
                 );

     }
}
