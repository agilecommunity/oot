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
        AppMetadata metadata = new AppMetadata();
        metadata.load();
        logger.debug("#appMetadata {}", metadata.getVersion());
        return ok(Json.toJson(metadata));
    }
  
    @AddCSRFToken
    public static Result index(String path) {
        AppMetadata metadata = new AppMetadata();
        metadata.load();
        return ok(views.html.main.render(metadata));
    }

    public static Result apiNotFound(String path) {
        return utils.controller.Results.notFoundError("指定したパスは存在しません");
    }
}
