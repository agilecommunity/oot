package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import filters.RequireCSRFCheck4Ng;
import play.Logger;
import play.data.validation.Constraints;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import scala.Option;
import securesocial.core.AuthenticationMethod;
import securesocial.core.BasicProfile;
import securesocial.core.java.Token;
import securesocial.core.java.UserAwareAction;
import securesocial.core.providers.MailToken;
import securesocial.core.providers.UsernamePasswordProvider;
import securesocial.core.providers.UsernamePasswordProvider$;
import securesocial.core.services.SaveMode;
import utils.controller.Results;
import utils.securesocial.BasicProfileBasedOperations;
import utils.securesocial.MailTokenBasedOperations;

import java.util.ArrayList;
import java.util.List;

public class Registration extends WithSecureSocialController {

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
    @UserAwareAction  // RuntimeEnvironmentを利用するために必要
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result startSignUp() {

        JsonNode json = request().body().asJson();
        Form<StartSignUpForm> filledForm = Form.form(StartSignUpForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#startSignUp hasErrors:" + filledForm.errorsAsJson());
            return Results.validationError(filledForm.errorsAsJson());
        }

        StartSignUpForm form = filledForm.get();
        String email = form.email.toLowerCase();

        Option<BasicProfile> mayBeUser = findByEmailWithUserpassProvider(email);

        if (mayBeUser.nonEmpty()) {
            getMailer().sendAlreadyRegisteredEmail(mayBeUser.get(), Http.Context.current()._requestHeader(), lang());
        } else {
            Token token = MailTokenBasedOperations.createToken(email, true);
            logger.debug("#startSignUp saveToken");
            getUserService().saveToken(token.toScala());
            logger.debug("#startSignUp sendSinupEmail");
            getMailer().sendSignUpEmail(email, token.uuid, Http.Context.current()._requestHeader(), lang());
        }

        return ok("");
    }

    @RequireCSRFCheck4Ng()
    @UserAwareAction  // RuntimeEnvironmentを利用するために必要
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result signUp(String token) {

        Option<MailToken> localToken = findToken(token);

        if (localToken.isEmpty()) {
            logger.debug("#signUp token not found token: {}", token);
            return Results.invalidLinkError(Messages.get("securesocial.signup.invalidLink"));
        }

        JsonNode json = request().body().asJson();

        logger.debug("#signUp request: {}", json.toString());

        Form<SignUpForm> filledForm = Form.form(SignUpForm.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#signUp hasErrors: {}", filledForm.errorsAsJson());
            return Results.validationError(filledForm.errorsAsJson());
        }

        SignUpForm form = filledForm.get();

        String email = localToken.get().email();
        String fullName = BasicProfileBasedOperations.createFullName(form.firstName, form.lastName);

        BasicProfile user = BasicProfileBasedOperations.createProfile(
            UsernamePasswordProvider$.MODULE$.UsernamePassword(),
            email,  // emailをIdとして利用する
            form.firstName,
            form.lastName,
            fullName,
            email,
            AuthenticationMethod.UserPassword(),
            form.passWord1
        );

        Ebean.beginTransaction();
        try {
            getUserService().save(user, SaveMode.SignUp());
            getUserService().deleteToken(token);

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }

        if (UsernamePasswordProvider.sendWelcomeEmail()) {
            getMailer().sendWelcomeEmail(user, Http.Context.current()._requestHeader(), lang());
        }

        return ok();
    }

}
