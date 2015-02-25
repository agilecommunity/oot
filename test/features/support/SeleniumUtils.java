package features.support;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SeleniumUtils {

    private static Integer DRIVER_WAIT = 10; // タイムアウト

    public static void waitForVisible(WebDriver driver, By locator) throws Throwable {
        Wait<WebDriver> wait = new WebDriverWait(driver, DRIVER_WAIT);
        wait.until(visibilityOfElementLocated(locator));
    }

    public static void waitForInvisible(WebDriver driver, By locator) throws Throwable {
        Wait<WebDriver> wait = new WebDriverWait(driver, DRIVER_WAIT);
        wait.until(invisibilityOfElementLocated(locator));
    }

    /**
     * 要素が現れるのを待ってから、クリックする
     * @param driver
     * @param locator
     * @throws Throwable
     */
    public static void waitAndClick(WebDriver driver, By locator) throws Throwable {
        SeleniumUtils.waitForVisible(driver, locator);
        WebElement target = driver.findElement(locator);
        target.click();
    }

    // findElement().clickでClickができないときの回避策
    public static void clickUsingJavaScript(WebDriver driver, By locator) throws Throwable {
        WebElement target = driver.findElement(locator);
        SeleniumUtils.clickUsingJavaScript(driver, target);
    }

    // findElement().clickでClickができないときの回避策
    public static void clickUsingJavaScript(WebDriver driver, WebElement target) throws Throwable {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("arguments[0].click();", target);
    }
}
