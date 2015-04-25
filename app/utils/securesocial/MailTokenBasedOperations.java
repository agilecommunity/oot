package utils.securesocial;

import org.joda.time.DateTime;
import play.api.Play;
import scala.Option;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.Token;
import securesocial.core.providers.MailToken;

import java.util.UUID;

public class MailTokenBasedOperations {

    public static Token createToken(String email, boolean isSignUp) {
        Integer duration = 60;
        Option<Object> configuredDuration = Play.current().configuration().getInt("securesocial.userpass.tokenDuration");
        if (configuredDuration.isDefined()) {
            duration = (Integer)configuredDuration.get();
        }

        Token token = new Token();
        token.creationTime = DateTime.now();
        token.email = email;
        token.expirationTime = token.creationTime.plusMinutes(duration);
        token.isSignUp = isSignUp;
        token.uuid = UUID.randomUUID().toString();

        return token;
    }

}
