package securesocial.custom;

import play.mvc.With;
import securesocial.core.java.*;
import securesocial.custom.services.AllAllowedAuthorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@With(MySecured.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MySecuredAction {
    /**
     * The Authorization implementation that checks if the user is allowed to execute this action.
     * By default, all requests are accepted.
     */
    Class<? extends Authorization> authorization() default AllAllowedAuthorization.class;

    /**
     * The responses sent when the invoker is not authorized or authenticated
     *
     * @see securesocial.core.java.DefaultSecuredActionResponses
     */
    Class<? extends SecuredActionResponses> responses() default MySecuredActionResponses.class;

    /**
     * The parameters that are passed to the Authorization.isAuthorized implementation
     */
    String[] params() default {};
}
