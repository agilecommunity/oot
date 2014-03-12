package controllers;

import play.api.mvc.*;

public class UIScripts {
    public static controllers.AssetsBuilder delegate = new controllers.AssetsBuilder();

    public static Action<AnyContent> at(String path, String file) {
        play.Logger.of("UIScripts").info(String.format("UIScripts.at %s %s", path, file));
        return delegate.at(path, file);
    }
}
