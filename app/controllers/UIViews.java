package controllers;

import play.Logger;
import play.api.mvc.*;

public class UIViews {
    private static Logger.ALogger logger = Logger.of("application.controllers.UIViews");

    public static controllers.AssetsBuilder delegate = new controllers.AssetsBuilder();

    public static Action<AnyContent> at(String path, String file) {

        logger.debug(String.format("#at path:%s file:%s", path, file));
        return delegate.at(path, file + ".html", false);
    }
}
