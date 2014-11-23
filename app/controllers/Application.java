package controllers;

import play.filters.csrf.AddCSRFToken;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
  
    @AddCSRFToken
    public static Result index(String path) {
        return ok(views.html.main.render());
    }

    public static Result apiNotFound(String path) {
        return notFound("指定したパスが存在しません");
    }
}
