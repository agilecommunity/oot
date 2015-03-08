package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import models.LocalToken;
import models.LocalUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import play.filters.csrf.CSRF;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;
import securesocial.core.Registry;
import utils.Utils;
import utils.snakeyaml.YamlUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

@RunWith(Enclosed.class)
public class PasswordResetTest {

    public static void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
        Utils.cleanUpDatabase();
        Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
    }

    public static class startResetとresetの利用方法 {

        @Before
        public void setUp() {
            PasswordResetTest.setUp();
        }

        @Test
        public void 確認() {

            String oldPassword = "hoehoehoe";
            String newPassword = "hogefuga";

            Map<String, String> params = new HashMap<String, String>();

            // 現行のパスワードでサインインできること
            params.clear();
            params.put("username", "steve@foo.bar");
            params.put("password", oldPassword);
            Result result = route(fakeRequest(POST, "/api/v1.0/authenticate/userpass")
                    .withFormUrlEncodedBody(params));
            assertThat(status(result)).describedAs("サインイン(現行パスワード)").isEqualTo(SEE_OTHER);

            params.put("email", "steve@foo.bar");
            JsonNode json = Json.toJson(params);
            result = Utils.callAPI(fakeRequest(POST, "/api/v1.0/start-reset").withJsonBody(json), "steve@foo.bar");

            assertThat(status(result)).describedAs("リセット開始").isEqualTo(OK);

            LocalToken token = LocalToken.find.where().eq("email", "steve@foo.bar").findUnique();

            params.clear();
            params.put("passWord1", newPassword);
            params.put("passWord2", newPassword);
            json = Json.toJson(params);
            result = Utils.callAPI(fakeRequest(POST, "/api/v1.0/reset/" + token.uuid).withJsonBody(json), "steve@foo.bar");

            assertThat(status(result)).describedAs("リセット").isEqualTo(OK);

            assertThat(LocalToken.find.where().eq("email", "steve@foo.bar").findRowCount()).isEqualTo(0);

            // 新しいパスワードでログインできること
            params.clear();
            params.put("username", "steve@foo.bar");
            params.put("password", newPassword);
            result = route(fakeRequest(POST, "/api/v1.0/authenticate/userpass")
                    .withFormUrlEncodedBody(params));

            assertThat(status(result)).describedAs("サインイン(新パスワード)").isEqualTo(SEE_OTHER);

            // 古いパスワードでログインできないこと
            params.clear();
            params.put("username", "steve@foo.bar");
            params.put("password", oldPassword);
            result = route(fakeRequest(POST, "/api/v1.0/authenticate/userpass")
                    .withFormUrlEncodedBody(params));
            assertThat(status(result)).describedAs("サインイン(現行パスワード)").isEqualTo(BAD_REQUEST);
        }
    }

    @RunWith(Parameterized.class)
    public static class resetに不正な値を指定した場合UnprocessableEntityが返ること {

        @Before
        public void setUp() {
            PasswordResetTest.setUp();

            LocalToken token = new LocalToken();
            token.email = "steve@foo.bar";
            token.isSignUp = true;
            token.uuid = "hoho";
            token.save();
        }

        @Parameterized.Parameters(name="{0}")
        public static Iterable<Object[]> getParameters() {
            return Arrays.asList(new Object[][]{
                    {new MyFixture("passWord1", "", "空")},
                    {new MyFixture("passWord2", "", "空")},
            });
        }

        @Parameterized.Parameter
        public MyFixture fixture;

        @Test
        public void テスト() {

            Map<String, String> params = new HashMap<String, String>();
            params.put("passWord1", "hogehoge");
            params.put("passWord2", "hogehoge");

            params.put(fixture.name, fixture.value);

            JsonNode json = Json.toJson(params);
            Result result = Utils.callAPI(fakeRequest(POST, "/api/v1.0/signup/hoho").withJsonBody(json), "steve@foo.bar");

            assertThat(status(result)).describedAs("リクエストの結果").isEqualTo(422);

            JsonNode errors = Json.parse(contentAsString(result));

            assertThat(errors.get("errors").get(fixture.name)).describedAs("エラーの項目").isNotEmpty();

        }

        static class MyFixture {
            public String name;
            public String value;
            public String description;

            public MyFixture(String name, String value, String description) {
                this.name = name;
                this.value = value;
                this.description = description;
            }

            @Override
            public String toString() {
                return String.format("項目:%s - %s", this.name, this.description);
            }
        }

    }

    public static class resetに登録されていないtokenを渡した場合Forbiddenが返ること {

        @Before
        public void setUp() {
            PasswordResetTest.setUp();

            LocalToken token = new LocalToken();
            token.email = "steve@foo.bar";
            token.isSignUp = false;
            token.uuid = "hoho";
            token.save();
        }

        @Test
        public void テスト() {

            Map<String, String> params = new HashMap<String, String>();
            params.put("passWord1", "hogehoge");
            params.put("passWord2", "hogehoge");

            JsonNode json = Json.toJson(params);
            Result result = Utils.callAPI(fakeRequest(POST, "/api/v1.0/signup/fufu").withJsonBody(json), "steve@foo.bar");

            assertThat(status(result)).describedAs("リクエストの結果").isEqualTo(FORBIDDEN);
        }
    }
}
