package controllers;

import static org.fest.assertions.api.Assertions.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.*;
import static play.test.Helpers.start;

import java.util.Arrays;
import java.util.List;

import models.DailyOrder;
import models.DailyOrderItem;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import org.junit.runners.Parameterized;
import play.Logger;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.filters.csrf.CSRF;
import play.mvc.Http.Cookie;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import utils.Utils;
import utils.controller.parameters.ParameterConverter;
import utils.snakeyaml.YamlUtil;

@RunWith(Enclosed.class)
public class DailyOrdersTest {

    Logger.ALogger logger = Logger.of("application.controllers.DailyOrdersTest");

    public static void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_order_item.yml"));
    }

    public static Result callUpdate(Long id, String jsonString) {

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

    public static Result callCreate(String jsonString) {

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

    public static Result callDelete(Long id) {

        Cookie fake_cookie = utils.Utils.fakeCookie("steve@foo.bar");
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        Result result = route(fakeRequest(DELETE, String.format("/api/v1.0/daily-orders/%d", id))
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));

        return result;
    }

    public static class createは受け取ったJsonの内容からDailyOrderオブジェクトを作成すること {
        @Before
        public void setUp() {
            DailyOrdersTest.setUp();
        }

        @Test
        public void 確認() {
            StringBuilder builder = new StringBuilder();
            builder.append("{ \"localUser\":{\"id\": \"steve@foo.bar\"}");
            builder.append(", \"orderDate\":\"2014-03-11T00:00:00.000+09:00\"");
            builder.append(", \"detailItems\":[{\"menuItem\":{\"id\":2}}]");
            builder.append("}");

            JsValue json = Json.parse(builder.toString());

            Result result = callCreate(builder.toString());

            assertThat(status(result)).isEqualTo(OK);

            DateTime dateValue = ParameterConverter.convertTimestampFrom("2014-03-11T00:00:00.000+09:00");
            DailyOrder order = DailyOrder.findBy(new DateTime(dateValue.getMillis()), "steve@foo.bar");
            assertThat(order.localUser.firstName).isEqualTo("スティーブ");

            assertThat(order.detailItems.size()).isEqualTo(1);
            DailyOrderItem order_item = order.detailItems.get(0);
            assertThat(order_item.menuItem.name).isEqualTo("たっぷりサーモン丼");
        }
    }

    public static class deleteは指定したIDのDailyOrderオブジェクトを削除すること {
        @Before
        public void setUp() {
            DailyOrdersTest.setUp();
        }

        @Test
        public void 確認() {

            assertThat(DailyOrder.find.byId(1L)).isNotNull();

            Result result = callDelete(1L);

            assertThat(status(result)).isEqualTo(OK);

            DailyOrder order = DailyOrder.find.byId(1L);

            assertThat(order).isNull();
        }
    }

    public static class updateは指定したIDのDailyOrderオブジェクトを更新すること {
        @Before
        public void setUp() {
            DailyOrdersTest.setUp();
        }

        @Test
        public void 確認() {
            StringBuilder builder = new StringBuilder();
            builder.append("{ \"id\":1");
            builder.append(", \"orderDate\":\"2014-02-10T00:00:00.000+09:00\"");
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
    }

    @RunWith(Parameterized.class)
    public static class createはJsonの内容が不正であった場合UnprocessableEntityを返すこと {
        @Before
        public void setUp() {
            DailyOrdersTest.setUp();
        }

        @Parameterized.Parameters(name="{0}")
        public static Iterable<Object[]> getParameters() {
            return Arrays.asList(new Object[][]{
                    {new MyFixture("{}", "空オブジェクト")},
                    {new MyFixture("{ \"localUser\": {\"id\":\"steve@foo.bar\"} }", "必須項目なし(orderDate)")},
                    {new MyFixture("{ \"orderDate\":\"2014-02-11T00:00:00.000+09:00\" }", "必須項目なし(localUser)")},
                    {new MyFixture("{ \"orderDate\":\"aaa\" }", "項目エラー(orderDate 存在しない日付)")}
            });
        }

        @Parameterized.Parameter
        public MyFixture fixture;

        @Test
        public void テスト() {
            Result result = callCreate(fixture.jsonString);
            assertThat(status(result)).isEqualTo(422);
        }

        static class MyFixture {
            public String jsonString;
            public String description;

            public MyFixture(String jsonString, String description) {
                this.jsonString = jsonString;
                this.description = description;
            }

            @Override
            public String toString() {
                return this.description;
            }
        }

    }

    @RunWith(Parameterized.class)
    public static class createはJsonの内容が不正であった場合Unauthorizedを返すこと {
        @Before
        public void setUp() {
            DailyOrdersTest.setUp();
        }

        @Parameterized.Parameters(name="{0}")
        public static Iterable<Object[]> getParameters() {
            return Arrays.asList(new Object[][]{
                    {new MyFixture("{ \"localUser\": {\"id\":\"hoge\"}, \"orderDate\":\"2014-02-11T00:00:00.000+09:00\" }", "ユーザが存在しない")},
                    {new MyFixture("{ \"localUser\": {\"id\":\"bob@foo.bar\"}, \"orderDate\":\"2014-02-10T00:00:00.000+09:00\" }", "所有者と異なるユーザ")},
            });
        }

        @Parameterized.Parameter
        public MyFixture fixture;

        @Test
        public void テスト() {
            Result result = callCreate(fixture.jsonString);
            assertThat(status(result)).isEqualTo(UNAUTHORIZED);
        }

        static class MyFixture {
            public String jsonString;
            public String description;

            public MyFixture(String jsonString, String description) {
                this.jsonString = jsonString;
                this.description = description;
            }

            @Override
            public String toString() {
                return this.description;
            }
        }

    }

    @RunWith(Parameterized.class)
    public static class createはJsonの内容が不正であった場合Conflictを返すこと {
        @Before
        public void setUp() {
            DailyOrdersTest.setUp();
        }

        @Parameterized.Parameters(name="{0}")
        public static Iterable<Object[]> getParameters() {
            return Arrays.asList(new Object[][]{
                    {new MyFixture("{ \"localUser\": {\"id\":\"steve@foo.bar\"}, \"orderDate\":\"2014-02-10T00:00:00.000+09:00\" }", "登録済みのオブジェクト")},
            });
        }

        @Parameterized.Parameter
        public MyFixture fixture;

        @Test
        public void テスト() {
            Result result = callCreate(fixture.jsonString);
            assertThat(status(result)).isEqualTo(CONFLICT);
        }

        static class MyFixture {
            public String jsonString;
            public String description;

            public MyFixture(String jsonString, String description) {
                this.jsonString = jsonString;
                this.description = description;
            }

            @Override
            public String toString() {
                return this.description;
            }
        }

    }
}
