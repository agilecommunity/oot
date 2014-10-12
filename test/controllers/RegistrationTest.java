package controllers;

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
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;
import static play.test.Helpers.POST;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class RegistrationTest {

    public static void setUp() {
        start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
    }

    public static class startSignupとsignupの利用方法 {

        @Before
        public void setUp() {
            RegistrationTest.setUp();
        }

        @Test
        public void 確認() {

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", "user1@foo.baa");
            JsonNode json = Json.toJson(params);
            Result result = callAPI(fakeRequest(POST, "/api/startSignup").withJsonBody(json));

            assertThat(status(result)).isEqualTo(OK);

            LocalToken token = LocalToken.find.where().eq("email", "user1@foo.baa").findUnique();

            params.clear();
            params.put("email", "user1@foo.baa");
            params.put("userName", "username1");
            params.put("firstName", "firstname1");
            params.put("lastName", "lastname1");
            params.put("passWord1", "hogehoge");
            params.put("passWord2", "hogehoge");
            json = Json.toJson(params);
            result = callAPI(fakeRequest(POST, "/api/signup/" + token.uuid).withJsonBody(json));

            assertThat(status(result)).isEqualTo(OK);

            assertThat(LocalToken.find.where().eq("email", "user1@foo.baa").findRowCount()).isEqualTo(0);
            assertThat(LocalUser.find.where().eq("email", "user1@foo.baa").findRowCount()).isEqualTo(1);
        }
    }

    @RunWith(Parameterized.class)
    public static class startSignupに不正な値を指定した場合_badRequestが返ること {

        @Before
        public void setUp() {
            RegistrationTest.setUp();
        }

        @Parameterized.Parameters(name="{0}")
        public static Iterable<Object[]> getParameters() {
            return Arrays.asList(new Object[][]{
                    {new MyFixture("", "email - 空")},
                    {new MyFixture("foo", "email - 不正な値")},
            });
        }

        @Parameterized.Parameter
        public MyFixture fixture;

        @Test
        public void テスト() {

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", this.fixture.name);
            JsonNode json = Json.toJson(params);
            Result result = callAPI(fakeRequest(POST, "/api/startSignup").withJsonBody(json));

            assertThat(status(result)).isEqualTo(BAD_REQUEST);

        }

        static class MyFixture {
            public String name;
            public String description;

            public MyFixture(String data, String description) {
                this.name = data;
                this.description = description;
            }

            @Override
            public String toString() {
                return this.description;
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class signupに不正な値を指定した場合_badRequestが返ること {

        @Before
        public void setUp() {
            RegistrationTest.setUp();

            LocalToken token = new LocalToken();
            token.email = "user1@foo.baa";
            token.is_sign_up = true;
            token.uuid = "hoho";
            token.save();
        }

        @Parameterized.Parameters(name="{0}")
        public static Iterable<Object[]> getParameters() {
            return Arrays.asList(new Object[][]{
                    {new MyFixture("email", "", "空")},
                    {new MyFixture("firstName", "", "空")},
                    {new MyFixture("lastName", "", "空")},
                    {new MyFixture("passWord1", "", "空")},
                    {new MyFixture("passWord2", "", "空")},
                    {new MyFixture("email", "foo", "不正な値")},
            });
        }

        @Parameterized.Parameter
        public MyFixture fixture;

        @Test
        public void テスト() {

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", "user1@foo.baa");
            params.put("userName", "username1");
            params.put("firstName", "firstname1");
            params.put("lastName", "lastname1");
            params.put("passWord1", "hogehoge");
            params.put("passWord2", "hogehoge");

            params.put(fixture.name, fixture.value);

            JsonNode json = Json.toJson(params);
            Result result = callAPI(fakeRequest(POST, "/api/signup/hoho").withJsonBody(json));

            assertThat(status(result)).describedAs("リクエストの結果").isEqualTo(BAD_REQUEST);

            JsonNode errors = Json.parse(contentAsString(result));

            assertThat(errors.get(fixture.name)).describedAs("エラーの項目").isNotEmpty();

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

    public static class signupに登録されていないtokenを渡した場合_forbiddenが返ること {

        @Before
        public void setUp() {
            RegistrationTest.setUp();

            LocalToken token = new LocalToken();
            token.email = "user1@foo.baa";
            token.is_sign_up = true;
            token.uuid = "hoho";
            token.save();
        }

        @Test
        public void テスト() {

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", "user1@foo.baa");
            params.put("userName", "username1");
            params.put("firstName", "firstname1");
            params.put("lastName", "lastname1");
            params.put("passWord1", "hogehoge");
            params.put("passWord2", "hogehoge");

            JsonNode json = Json.toJson(params);
            Result result = callAPI(fakeRequest(POST, "/api/signup/fufu").withJsonBody(json));

            assertThat(status(result)).describedAs("リクエストの結果").isEqualTo(FORBIDDEN);
        }


    }

    private static Result callAPI(FakeRequest baseRequest) {
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }

}
