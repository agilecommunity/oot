package securesocial.custom;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredActionResponses;

public class MySecuredActionResponses extends Controller implements SecuredActionResponses {
    public Html notAuthorizedPage(Http.Context ctx) {
        return securesocial.views.html.notAuthorized.render(ctx._requestHeader(), ctx.lang(), SecureSocial.env());
    }

    public F.Promise<Result> notAuthenticatedResult(Http.Context ctx) {
        Http.Request req = ctx.request();
        Result result;

        if ( req.accepts("application/json")) {
            ObjectNode node = Json.newObject();
            node.put("error", "Credentials required");
            result = unauthorized(node);
        } else if ( req.accepts("text/html")) {
            ctx.flash().put("error", play.i18n.Messages.get("securesocial.loginRequired"));
            ctx.session().put(SecureSocial.ORIGINAL_URL, ctx.request().uri());
            result = redirect(SecureSocial.env().routes().loginPageUrl(ctx._requestHeader()));
        } else {
            result = unauthorized("Credentials required");
        }
        return F.Promise.pure(result);
    }

    public F.Promise<Result> notAuthorizedResult(Http.Context ctx) {
        Http.Request req = ctx.request();
        Result result;

        if ( req.accepts("application/json")) {
            ObjectNode node = Json.newObject();
            node.put("error", "Not authorized");
            result = forbidden(node);
        } else if ( req.accepts("text/html")) {
            result = forbidden(notAuthorizedPage(ctx));
        } else {
            result = forbidden("Not authorized");
        }

        return F.Promise.pure(result);
    }
}
