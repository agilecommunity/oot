package controllers;

import models.LocalUser;
import play.mvc.Controller;
import play.mvc.Http;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import securesocial.core.BasicProfile;
import securesocial.core.authenticator.Authenticator;
import securesocial.core.authenticator.AuthenticatorBuilder;
import securesocial.core.java.SecureSocial;
import securesocial.core.providers.MailToken;
import securesocial.core.providers.UsernamePasswordProvider$;
import securesocial.core.providers.utils.Mailer;
import securesocial.core.services.RoutesService;
import securesocial.core.services.UserService;

import java.util.concurrent.TimeUnit;

public class WithSecureSocialController extends Controller {

    public static UserService<LocalUser> getUserService() {
        return SecureSocial.env().userService();
    }

    public static RoutesService getRouteService() {
        return SecureSocial.env().routes();
    }

    public static Mailer getMailer() {
        return SecureSocial.env().mailer();
    }

    public static LocalUser getCurrentUser() {
        return (LocalUser)ctx().args.get(SecureSocial.USER_KEY);
    }

    public static Authenticator<LocalUser> getAuthenticator(String id) {

        Option<AuthenticatorBuilder<LocalUser>> maybeBuilder = SecureSocial.env().authenticatorService().find(id);

        if (maybeBuilder.isEmpty()) {
            throw new RuntimeException("Configuration Error. AuthenticatorBuilder not found");
        }

        AuthenticatorBuilder<LocalUser> builder = maybeBuilder.get();

        Future<Option<Authenticator<LocalUser>>> futureAuthenticator = builder.fromRequest(Http.Context.current()._requestHeader());

        try {
            Await.result(futureAuthenticator, Duration.create(10, TimeUnit.MILLISECONDS));
        } catch (Exception ex) {
            throw new RuntimeException("failed to wait future", ex);
        }

        Option<Authenticator<LocalUser>> mayBeAuthenticator = futureAuthenticator.value().get().get();

        if (mayBeAuthenticator.isEmpty()) {
            throw new RuntimeException("Configuration Error. Authenticator not found");
        }

        return mayBeAuthenticator.get();
    }

    public static Option<BasicProfile> findByEmailWithUserpassProvider(String email) {
        return WithSecureSocialController.findByEmailAndProvider(email, UsernamePasswordProvider$.MODULE$.UsernamePassword());
    }

    public static Option<BasicProfile> findByEmailAndProvider(String email, String providerId) {
        Future<Option<BasicProfile>> futureUser = WithSecureSocialController.getUserService().findByEmailAndProvider(email, providerId);

        try {
            Await.result(futureUser, Duration.create(10, TimeUnit.MILLISECONDS));
        } catch (Exception ex) {
            throw new RuntimeException("failed to wait future", ex);
        }

        return futureUser.value().get().get();
    }

    public static Option<MailToken> findToken(String token) {
        Future<Option<MailToken>> futureToken = WithSecureSocialController.getUserService().findToken(token);

        try {
            Await.result(futureToken, Duration.create(10, TimeUnit.MILLISECONDS));
        } catch (Exception ex) {
            throw new RuntimeException("failed to wait future", ex);
        }

        return futureToken.value().get().get();
    }
}
