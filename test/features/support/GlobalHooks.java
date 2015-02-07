package features.support;

import static play.test.Helpers.*;

import java.util.List;

import play.Logger;
import play.libs.Yaml;
import play.test.TestServer;

import com.avaje.ebean.Ebean;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import utils.Utils;

public class GlobalHooks {

    private static Logger.ALogger logger = Logger.of("application.features.support.GlobalHooks");

    public static int PORT = 3333;
    private static TestServer TEST_SERVER;

    @Before
    public void before() {

        WebBrowser.setUp();

        TEST_SERVER = testServer(PORT, fakeApplication());
        start(TEST_SERVER);

        Utils.cleanUpDatabase();
    }

    @After
    public void after() {
        logger.debug("#after");

        stop(TEST_SERVER);
    }
}
