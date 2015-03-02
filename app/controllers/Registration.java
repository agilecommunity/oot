package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import filters.RequireCSRFCheck4Ng;
import org.joda.time.DateTime;
import play.Logger;
import play.api.Play;
import play.data.validation.Constraints;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.libs.Scala;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.Option;
import securesocial.core.*;
import securesocial.core.java.Token;
import securesocial.core.providers.UsernamePasswordProvider;
import securesocial.core.providers.UsernamePasswordProvider$;
import securesocial.core.providers.utils.GravatarHelper$;
import securesocial.core.providers.utils.Mailer$;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Registration extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.Registration");

    public static class StartSignUpForm {
        @Constraints.Required
        @Constraints.Email
        public String email;
    }

    public static class SignUpForm {
        @Constraints.Required
        public String firstName;

        @Constraints.Required
        public String lastName;

        @Constraints.Required
        public String passWord1; // securesocialのデフォルトはpassword.password1で指定する

        @Constraints.Required
        public String passWord2;

        public List<ValidationError> validate() {

            if (this.passWord1 == null && this.passWord2 == null) {
                return null;
            }

            if (this.passWord1 != null && this.passWord1.equals(this.passWord2)) {
                return null;
            }

            ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
            errors.add(new ValidationError("passWord2", "securesocial.signup.passwordsDoNotMatch"));
            return errors;
        }
    }

    @RequireCSRFCheck4Ng()
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result startSignUp() {

        JsonNode json = request().body().asJson();
        Form<StartSignUpForm> filledForm = Form.form(StartSignUpForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#startSignUp hasErrors:" + filledForm.errorsAsJson());
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        StartSignUpForm form = filledForm.get();
        String email = form.email.toLowerCase();

        Option<Identity> maybeUser = UserService$.MODULE$.findByEmailAndProvider(email, UsernamePasswordProvider$.MODULE$.UsernamePassword());

        if (maybeUser.nonEmpty()) {
            Mailer$.MODULE$.sendAlreadyRegisteredEmail(maybeUser.get(), Http.Context.current()._requestHeader());
        } else {
            Token token = createToken(email);
            UserService$.MODULE$.save(token.toScala());
            Mailer$.MODULE$.sendSignUpEmail(email, token.uuid, Http.Context.current()._requestHeader());
        }

        return ok("");
    }

    @RequireCSRFCheck4Ng()
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result signUp(String token) {

        Option<securesocial.core.providers.Token> localToken = UserService$.MODULE$.findToken(token);

        if (localToken.isEmpty()) {
            logger.debug("#signUp token not found token:" + token);
            return utils.controller.Results.invalidLinkError(Messages.get("securesocial.signup.invalidLink"));
        }

        JsonNode json = request().body().asJson();

        logger.debug("#signUp request:" + json.toString());

        Form<SignUpForm> filledForm = Form.form(SignUpForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#signUp hasErrors:" + filledForm.errorsAsJson());
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        SignUpForm form = filledForm.get();

        String id = localToken.get().email();
        IdentityId identityId = new IdentityId(id, "userpass");

        SocialUser user = new SocialUser(
            identityId,
            form.firstName,
            form.lastName,
            String.format("%s %s", form.firstName, form.lastName),
            Scala.Option(id),
            GravatarHelper$.MODULE$.avatarFor(id),
            AuthenticationMethod.UserPassword(),
            null,
            null,
            Scala.Option(Registry.hashers().get("bcrypt").get().hash(form.passWord1)) // カレントとるのどうやるの??
        );

        Ebean.beginTransaction();
        try {
            UserService$.MODULE$.save(user);
            UserService$.MODULE$.deleteToken(token);

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }

        if (UsernamePasswordProvider.sendWelcomeEmail()) {
            Mailer$.MODULE$.sendWelcomeEmail(user, Http.Context.current()._requestHeader());
        }

        return ok();
    }

    protected static Token createToken(String email) {
        Integer duration = 60;
        Option<Object> configuredDuration = Play.current().configuration().getInt("securesocial.userpass.tokenDuration");
        if (configuredDuration.isDefined()) {
            duration = (Integer)configuredDuration.get();
        }

        Token token = new Token();
        token.creationTime = DateTime.now();
        token.email = email;
        token.expirationTime = token.creationTime.plusMinutes(duration);
        token.isSignUp = true;
        token.uuid = UUID.randomUUID().toString();

        return token;
    }

}
