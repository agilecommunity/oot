package features.support;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SeleniumUtils {

    private static Integer DRIVER_WAIT = 10; // タイムアウト

    public static void waitForVisible(org.openqa.selenium.WebDriver driver, By locator) throws Throwable {
        Wait<WebDriver> wait = new WebDriverWait(driver, DRIVER_WAIT);
        wait.until(visibilityOfElementLocated(locator));
    }


}
