package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import filters.RequireCSRFCheck4Ng;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
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
import securesocial.core.providers.UsernamePasswordProvider$;
import securesocial.core.providers.utils.GravatarHelper$;
import securesocial.core.providers.utils.Mailer$;
import utils.controller.MailTokenBasedOperations;

import java.util.ArrayList;
import java.util.List;

public class PasswordReset extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.PasswordReset");

    public static class StartResetForm {
        @Constraints.Required
        @Constraints.Email
        public String email;
    }

    public static class ResetForm {
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
    public static Result startReset() {

        JsonNode json = request().body().asJson();
        Form<StartResetForm> filledForm = Form.form(StartResetForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#startReset hasErrors:" + filledForm.errorsAsJson());
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        StartResetForm form = filledForm.get();
        String email = form.email.toLowerCase();

        Option<Identity> maybeUser = UserService$.MODULE$.findByEmailAndProvider(email, UsernamePasswordProvider$.MODULE$.UsernamePassword());

        if (maybeUser.nonEmpty()) {
            Token token = MailTokenBasedOperations.createToken(email, false);
            UserService$.MODULE$.save(token.toScala());
            Mailer$.MODULE$.sendPasswordResetEmail(maybeUser.get(), token.uuid, Http.Context.current()._requestHeader());
        } else {
            Mailer$.MODULE$.sendUnkownEmailNotice(email, Http.Context.current()._requestHeader());
        }

        return ok("");
    }

    @RequireCSRFCheck4Ng()
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result reset(String token) {

        Option<securesocial.core.providers.Token> localToken = UserService$.MODULE$.findToken(token);

        if (localToken.isEmpty()) {
            logger.debug("#reset token not found token:" + token);
            return utils.controller.Results.invalidLinkError(Messages.get("securesocial.signup.invalidLink"));
        }

        JsonNode json = request().body().asJson();

        logger.debug("#reset request:" + json.toString());

        Form<ResetForm> filledForm = Form.form(ResetForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#reset hasErrors:" + filledForm.errorsAsJson());
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        ResetForm form = filledForm.get();

        String email = localToken.get().email();

        Option<Identity> maybeUser = UserService$.MODULE$.findByEmailAndProvider(email, UsernamePasswordProvider$.MODULE$.UsernamePassword());

        if (maybeUser.isEmpty()) {
            logger.error("#reset could not find user with email {} during password reset", email);
            return utils.controller.Results.resourceNotFoundError();
        }

        Identity profile = maybeUser.get();
        String id = localToken.get().email();

        SocialUser user = new SocialUser(
            profile.identityId(),
            profile.firstName(),
            profile.lastName(),
            profile.fullName(),
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

        Mailer$.MODULE$.sendPasswordChangedNotice(user, Http.Context.current()._requestHeader());

        return ok();
    }
}
