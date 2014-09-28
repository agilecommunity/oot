package controllers;

import eu.medsea.mimeutil.MimeUtil;
import models.LocalUser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class MenuItemImages extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.MenuItemImages");

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result create() {
        logger.debug("#create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        String contentType = request().getHeader("Content-Type");

        logger.debug(String.format("#create Content-Type: %s", contentType));

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.is_admin) {
            logger.warn(String.format("#create only admin can create. local_user.id:%s", localUser.email));
            return unauthorized();
        }

        Http.MultipartFormData formData = request().body().asMultipartFormData();

        if (formData.getFiles().size() == 0) {
            logger.debug("#create no files");
            return badRequest("");
        }

        File mayBeZipFile = formData.getFile("menuItemImages").getFile();
        logger.debug(String.format("#create fileName:%s", mayBeZipFile.getName()));

        if (!isZipFile(mayBeZipFile)) {
            logger.debug("#create file isn't zip");
            return badRequest();
        }

        try {
            extractImages(mayBeZipFile);
        } catch (IOException e) {
            return internalServerError();
        } catch (ZipException e) {
            return internalServerError();
        }

        return ok("");
    }

    private static boolean isZipFile(File mayBeZipFile) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");

        Collection<String> mimeTypes = MimeUtil.getMimeTypes(mayBeZipFile);

        logger.debug(String.format("#isZipFile file mimeTypes:%s contains(zip):%s", mimeTypes.toString(), mimeTypes.contains("application/zip")));

        return mimeTypes.contains("application/zip");
    }

    private static void extractImages(File mayBeZipFile) throws IOException, ZipException {
        String pathToImages = Play.application().path().getAbsolutePath() + "/public/images/menu-items";
        String pathToWork = Play.application().path().getAbsolutePath() + "/.tmp";

        try {
            ZipFile zipFile = new ZipFile(mayBeZipFile);
            zipFile.setFileNameCharset("MS932");

            List<FileHeader> headerList = zipFile.getFileHeaders();
            for (FileHeader header : headerList) {
                logger.debug(header.getFileName());

                extractImage(zipFile, header.getFileName(), pathToImages, pathToWork);
            }
        } catch (ZipException e) {
            logger.error(String.format("#extractImages zip operation error:%s", e.getLocalizedMessage()));
            throw e;
        } finally {
            try {
                FileUtils.deleteDirectory(new File(pathToWork));
            } catch (IOException e) {
                logger.warn(String.format("#extractImages failed to delete work directory. error:%s", e.getLocalizedMessage()));
                throw e;
            }
        }
    }

    private static void extractImage(ZipFile zipFile, String fileName, String pathToExtract, String pathToWork) throws ZipException, IOException {

        if (fileName.endsWith(".png")) {
            zipFile.extractFile(fileName, pathToExtract);
            return;
        }

        zipFile.extractFile(fileName, pathToWork);
        try {
            BufferedImage currentImage = ImageIO.read(new File(pathToWork + "/" + fileName));
            BufferedImage newImage = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            newImage.getGraphics().drawImage(currentImage, 0, 0, null);

            String newFileName = FilenameUtils.getBaseName(fileName);
            File converted = new File(pathToExtract + "/" + newFileName + ".png");
            ImageIO.write(newImage, "png", converted);
        } catch (IOException e) {
            logger.error(String.format("#extractImage image conversion error:%s", e.getLocalizedMessage()));
            throw e;
        }
    }

}
