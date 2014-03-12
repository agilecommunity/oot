package controllers;

import play.api.mvc.*;

public class UIViews {
    public static controllers.AssetsBuilder delegate = new controllers.AssetsBuilder();

    public static Action<AnyContent> at(String path, String file) {
        play.Logger.of("UIViews").info(String.format("UIViews.at %s %s", path, file));
        return delegate.at(path, file + ".html");
    }
}
