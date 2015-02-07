package controllers;

import models.AppMetadata;
import play.Logger;
import play.filters.csrf.AddCSRFToken;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.Application");

    public static Result appMetadata() {
        logger.debug("#appMetadata {}", AppMetadata.getInstance().version);
        return ok(Json.toJson(AppMetadata.getInstance()));
    }
  
    @AddCSRFToken
    public static Result index(String path) {
        return ok(views.html.main.render());
    }

    public static Result apiNotFound(String path) {
        return notFound("指定したパスが存在しません");
    }
}
