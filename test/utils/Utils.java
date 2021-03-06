package utils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import models.AppMetadata;
import models.LocalUser;
import org.yaml.snakeyaml.Yaml;
import play.Logger;
import play.Play;
import play.filters.csrf.CSRF;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.test.FakeRequest;
import play.libs.Scala;
import scala.Option;
import scala.concurrent.Future;
import scala.util.Either;
import securesocial.core.AuthenticationMethod;
import securesocial.core.BasicProfile;
import securesocial.core.authenticator.AuthenticatorBuilder;
import securesocial.core.authenticator.CookieAuthenticator;
import securesocial.core.authenticator.CookieAuthenticator$;
import securesocial.core.java.SecureSocial;
import securesocial.core.providers.utils.PasswordHasher;
import securesocial.core.PasswordInfo;
import utils.securesocial.BasicProfileBasedOperations;
import securesocial.core.RuntimeEnvironment;
import securesocial.custom.services.MyEnvironment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;

public class Utils {

    private static final Logger.ALogger logger = Logger.of("test.Utils");

    public static Cookie fakeCookie(String id){

        LocalUser localUser = LocalUser.find.byId(id);

        Logger.debug(String.format("LocalUser id:%s provider:%s", localUser.id, localUser.provider));

        Map<String, String> authParams = new HashMap();
        authParams.put("username", localUser.id);
        authParams.put("password", "hoehoehoe");

        FakeRequest authRequest = fakeRequest(POST, "/api/v1.0/signin/userpass")
                .withFormUrlEncodedBody(authParams);

        Result result = route(authRequest);

        logger.trace("#fakeCookie headers: {}", headers(result));

        Cookie authCookie = cookies(result).get("id");

        return authCookie;
    }

    /**
     * リクエストに指定したユーザの認証データとXSRF-TOKENを付与して呼び出します。
     * @param baseRequest
     * @param userId
     * @return
     */
    public static Result callAPI(FakeRequest baseRequest, String userId) {
        Http.Cookie fake_cookie = utils.Utils.fakeCookie(userId);
        String token = CSRF.SignedTokenProvider$.MODULE$.generateToken();

        return route(baseRequest
                .withCookies(fake_cookie)
                .withHeader("X-XSRF-TOKEN", token)
                .withSession("XSRF-TOKEN", token));
    }


    public static Map<String, String> getAdditionalApplicationSettings() {
        Map<String, String> settings = new HashMap<String, String>();
        return settings;
    }

    public static void cleanUpDatabase() {
        Transaction trans = Ebean.beginTransaction();
        try {
            Connection conn = trans.getConnection();

            cleanUpTables(conn);
            initializeSequence(conn);

            Ebean.commitTransaction();
        } catch (SQLException ex) {
            logger.error("#cleanUpDatabase", ex);
        } finally {
            Ebean.endTransaction();
        }
    }

    public static void createAppMetadata(Map<String, Object> data) throws Throwable {

        File mine = new File(Utils.class.getResource("Utils.class").getPath());
        String currentAppDir = Paths.get(mine.getParent(), "../../../../").normalize().toString();

        File metaFile = new File(Paths.get(currentAppDir, "target/scala-2.11/classes" , AppMetadata.pathToMetadata).toString());

        if (metaFile.exists()) {
            metaFile.delete();
            metaFile.createNewFile();
        }

        Writer writer = new OutputStreamWriter(new FileOutputStream(metaFile), "UTF8");

        Yaml yaml = new Yaml();
        yaml.dump(data, writer);
        writer.close();
    }

    private static void cleanUpTables(Connection conn) throws SQLException {
        String[] tables = {
                "DAILY_MENU",
                "DAILY_MENU_ITEM",
                "DAILY_ORDER",
                "DAILY_ORDER_ITEM",
                "LOCAL_TOKEN",
                "LOCAL_USER",
                "MENU_ITEM",
                "GATHERING_SETTING"
        };

        for (String table : tables) {
            String query = String.format("TRUNCATE TABLE %s", table);
            logger.debug("query: {}", query);
            conn.createStatement().executeUpdate(query);
        }
    }

    private static void initializeSequence(Connection conn) throws SQLException {

        String[] sequences = {
                "DAILY_MENU_SEQ",
                "DAILY_MENU_ITEM_SEQ",
                "DAILY_ORDER_SEQ",
                "DAILY_ORDER_ITEM_SEQ",
                "MENU_ITEM_SEQ",
                "GATHERING_SETTING_SEQ"
        };

        Boolean isH2 = false;
        String dbUrl = conn.getMetaData().getURL();
        if (dbUrl == null || dbUrl.contains(":h2:") == true) {
            isH2 = true;
        }

        for (String sequence : sequences) {

            if (isH2) {
                String query = String.format("ALTER SEQUENCE %s RESTART WITH 1", sequence);
                conn.createStatement().executeUpdate(query);
            } else {
                String query = String.format("SELECT setval('%s', 1, false)", sequence);
                conn.createStatement().executeQuery(query);
            }

        }
    }
}