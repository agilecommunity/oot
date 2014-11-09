package controllers;

import org.apache.commons.io.FilenameUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;

public class UCAssets extends Controller {
    private static Logger.ALogger logger = Logger.of("application.controllers.UCAssets");

    private static String RootPath = "uc-assets";

    public static Result at(String file) {

        String targetFilePath = UCAssets.pathTo(file);

        logger.trace(String.format("#at target file path:%s", targetFilePath));

        File targetFile = new File(targetFilePath);

        if (!targetFile.exists()) {
            return notFound();
        }

        return ok(targetFile);
    }

    public static String pathTo(String file) {

        String ucAssetsRoot = UCAssets.rootAbsolutePath();

        logger.trace(String.format("#pathTo uc-assets root:%s", ucAssetsRoot));

        String targetFilePath = FilenameUtils.concat(ucAssetsRoot, file);

        logger.trace(String.format("#pathTo target file path:%s", targetFilePath));

        return targetFilePath;
    }

    public static String rootAbsolutePath() {

        String ucAssetsRoot = FilenameUtils.concat(Play.application().path().getAbsolutePath(), UCAssets.RootPath);

        logger.trace(String.format("#rootAbsolutePath uc-assets root:%s", ucAssetsRoot));

        return ucAssetsRoot;
    }
}
