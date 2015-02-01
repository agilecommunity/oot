package utils;

import com.avaje.ebean.Ebean;
import models.LocalUser;
import play.Logger;
import play.mvc.Http.Cookie;
import scala.Option;
import scala.Some;
import scala.util.Either;
import securesocial.core.AuthenticationMethod;
import securesocial.core.Authenticator;
import securesocial.core.IdentityId;
import securesocial.core.PasswordInfo;
import securesocial.core.SocialUser;
import utils.snakeyaml.JodaPropertyConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.inMemoryDatabase;

/*
 * java - Unit-testing methods secured with Securesocial annotation - Stack Overflow
 * http://stackoverflow.com/questions/19226110/unit-testing-methods-secured-with-securesocial-annotation
 * より拝借
 */
public class Utils {

    Logger.ALogger logger = Logger.of("test.Utils");

    public static Cookie fakeCookie(String id){

        LocalUser local_user = LocalUser.find.byId(id);

        Logger.debug(String.format("LocalUser id:%s provider:%s", local_user.id, local_user.provider));

        SocialUser social_user = new SocialUser(new IdentityId(local_user.id, local_user.provider),
            local_user.firstName,
            local_user.lastName,
            String.format("%s %s", local_user.firstName, local_user.lastName),
            Option.apply(local_user.email),
            null,
            new AuthenticationMethod("userPassword"),
            null,
            null,
            Some.apply(new PasswordInfo("bcrypt", local_user.password, null))
        );

        Either either = Authenticator.create(social_user);
        Authenticator auth = (Authenticator) either.right().get();
        play.api.mvc.Cookie scala_cookie = auth.toCookie();

        // secureSocial doesnt seem to set a maxAge or Domain so i set them myself.
        Cookie fake_cookie = new Cookie(auth.cookieName(), scala_cookie.value(), 120, scala_cookie.path(), "None", auth.cookieSecure(), auth.cookieHttpOnly());
        return fake_cookie;
    }

    public static Map<String, String> getAdditionalApplicationSettings() {
        Map<String, String> settings = new HashMap<String, String>();
        settings.putAll(inMemoryDatabase());

        return settings;
    }

    public static Object loadYaml(String pathToFile) {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(new JodaPropertyConstructor());
        return yaml.load(play.Play.application().resourceAsStream(pathToFile));
    }
}