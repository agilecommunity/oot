package models;

import controllers.UCAssets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import play.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class MenuItemImage {

    public static final String ImageFormatName = "png";

    private static Logger.ALogger logger = Logger.of("application.models.MenuItemImage");

    private MenuItem menuItem;
    private byte[] image;

    public MenuItemImage(MenuItem menuItem) {
        if (menuItem == null || menuItem.id == null) {
            throw new RuntimeException("menuItem should not be null");
        }

        this.menuItem = menuItem;
    }

    public void setImageBase64(String base64Image) throws IOException {
        this.image = Base64.decodeBase64(base64Image.getBytes());
    }

    public void save() throws IOException {

        ByteArrayInputStream input = new ByteArrayInputStream(this.image);
        BufferedImage tmpImage = ImageIO.read(input);

        BufferedImage newImage = new BufferedImage(tmpImage.getWidth(), tmpImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        newImage.getGraphics().drawImage(tmpImage, 0, 0, null);

        String imageFileName = this.getFileName(this.menuItem, this.image);
        String imageFilePath = this.getFilePath(this.menuItem, this.image);

        logger.debug("#save path: {}", imageFilePath);

        File directory = new File(this.getImageDirectory());
        if (!directory.exists()) {
            logger.error("#save Image Directory does not exist path: {}", this.getImageDirectory());
            throw new IOException("Image Directory does not exist.");
        }

        File converted = new File(imageFilePath);
        ImageIO.write(newImage, ImageFormatName, converted);

        // 古いファイルを削除
        this.deleteFile(menuItem);

        this.menuItem.itemImagePath = imageFileName;
    }

    private String getFilePath(MenuItem item, byte[] image) {
        return FilenameUtils.concat(this.getImageDirectory(), this.getFileName(item, image));
    }

    private String getImageDirectory() {
        String pathToImages = FilenameUtils.concat(UCAssets.rootAbsolutePath(), "images");
        return FilenameUtils.concat(pathToImages, "menu-items");
    }

    private String getFileNameBase(MenuItem item, byte[] image) {
        String fingerPrint = DigestUtils.md5Hex(image);
        return String.format("%s-%s", this.getFileNameBase(item), fingerPrint);
    }

    private String getFileNameBase(MenuItem item) {
        return String.format("%010d", menuItem.id);
    }

    private String getFileName(MenuItem item, byte[] image) {
        return String.format("%s.%s", this.getFileNameBase(item, image), ImageFormatName);
    }

    private void deleteFile(MenuItem item) {
        if (item.itemImagePath == null || item.itemImagePath.isEmpty()) {
            return;
        }

        String imageDirectory = this.getImageDirectory();
        String imagePath = FilenameUtils.concat(imageDirectory, item.itemImagePath);
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            logger.debug("#deleteFile path: {}", imagePath);
            imageFile.delete();
        }
    }
}
