package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import filters.RequireCSRFCheck4Ng;
import models.LocalUser;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import scala.Option;
import securesocial.core.AuthenticationMethod;
import securesocial.core.BasicProfile;
import securesocial.core.Events;
import securesocial.core.PasswordResetEvent;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.Token;
import securesocial.core.java.UserAwareAction;
import securesocial.core.providers.MailToken;
import securesocial.core.services.SaveMode;
import utils.controller.Results;
import utils.securesocial.BasicProfileBasedOperations;
import utils.securesocial.MailTokenBasedOperations;

import java.util.ArrayList;
import java.util.List;

public class PasswordReset extends WithSecureSocialController {

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
    @UserAwareAction  // RuntimeEnvironmentを利用するために必要
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result startReset() {

        JsonNode json = request().body().asJson();
        Form<StartResetForm> filledForm = Form.form(StartResetForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#startReset hasErrors: {}", filledForm.errorsAsJson());
            return Results.validationError(filledForm.errorsAsJson());
        }

        StartResetForm form = filledForm.get();
        String email = form.email;

        Option<BasicProfile> maybeUser = findByEmailWithUserpassProvider(email);

        if (maybeUser.nonEmpty()) {
            Token token = MailTokenBasedOperations.createToken(email, false);
            getUserService().saveToken(token.toScala());
            getMailer().sendPasswordResetEmail(maybeUser.get(), token.uuid, Http.Context.current()._requestHeader(), lang());
        } else {
            getMailer().sendUnkownEmailNotice(email, Http.Context.current()._requestHeader(), lang());
        }

        return ok("");
    }

    @RequireCSRFCheck4Ng()
    @UserAwareAction  // RuntimeEnvironmentを利用するために必要
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result reset(String token) {

        Option<MailToken> localToken = findToken(token);

        if (localToken.isEmpty()) {
            logger.debug("#reset token not found token: {}", token);
            return Results.invalidLinkError(Messages.get("securesocial.signup.invalidLink"));
        }

        JsonNode json = request().body().asJson();

        if (!Play.isProd()) {
            logger.debug("#reset request: {}", json.toString());
        }

        Form<ResetForm> filledForm = Form.form(ResetForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#reset hasErrors: {}", filledForm.errorsAsJson());
            return Results.validationError(filledForm.errorsAsJson());
        }

        ResetForm form = filledForm.get();

        String email = localToken.get().email();

        Option<BasicProfile> maybeUser = findByEmailWithUserpassProvider(email);

        if (maybeUser.isEmpty()) {
            logger.error("#reset could not find user with email {} during password reset", email);
            return Results.resourceNotFoundError();
        }

        BasicProfile profile = maybeUser.get();

        if (!Play.isProd()) {
            logger.debug("#reset password: {}", form.passWord1);
        }

        BasicProfile newProfile = BasicProfileBasedOperations.createProfile(
            profile.providerId(),
            profile.userId(),
            profile.firstName().get(),
            profile.lastName().get(),
            profile.fullName().get(),
            profile.email().get(),
            AuthenticationMethod.UserPassword(),
            form.passWord1
        );

        if (Play.isTest()) {
            logger.debug("#reset basicprofile password: {}", newProfile.passwordInfo().get().password());
        }

        Ebean.beginTransaction();
        try {
            getUserService().save(newProfile, SaveMode.PasswordChange());
            getUserService().deleteToken(token);

            Ebean.commitTransaction();

            Option<Http.Session> newSession = Events.fire(new PasswordResetEvent(newProfile), Http.Context.current()._requestHeader(), SecureSocial.env());
            logger.debug("#reset eventSession: {}", newSession);
        } catch (Exception ex) {
            logger.error("#reset failed to update", ex);
            Ebean.rollbackTransaction();
        } finally {
            Ebean.endTransaction();
        }

        if (!Play.isProd()) {
            LocalUser localUser = LocalUser.findbyProviderIdAndUserId(newProfile.providerId(), newProfile.userId());
            logger.debug("#reset after updated provider: {}", localUser.provider);
            logger.debug("#reset after updated password: {}", localUser.password);
        }

        getMailer().sendPasswordChangedNotice(newProfile, Http.Context.current()._requestHeader(), lang());

        return ok();
    }
}
