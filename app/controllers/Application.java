package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
  
  public static Result index(String path) {
    return ok(views.html.main.render());
  }
  
}
