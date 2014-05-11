package features.stepdefs.common;

import static org.fest.assertions.Assertions.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import features.support.WebBrowser;

public class WebStepDefs {

    public void アクセスする(String url) throws Throwable {
        WebBrowser.INSTANCE.get(url);
    }

    public void フィールド_に_を入力する(String fieldId, String value) throws Throwable {
        WebBrowser.INSTANCE.findElement(By.id(fieldId)).sendKeys(value);
    }

    public void ボタンをクリックする(String buttonId) throws Throwable {
        this.ボタンをクリックする(By.id(buttonId));
    }

    public void ボタンをクリックする(By locator) throws Throwable {
        // findElement().clickでClickができないときの回避策
        WebElement target = WebBrowser.INSTANCE.findElement(locator);
        JavascriptExecutor js = (JavascriptExecutor)WebBrowser.INSTANCE;
        js.executeScript("arguments[0].click();", target);
    }

    public void ミリ秒待つ(Long milliSec) throws Throwable {
        Thread.sleep(milliSec);
    }

    public void 現れるまで待つ(String elementId) throws Throwable {
        this.現れるまで待つ(By.id(elementId));
    }

    public void 現れるまで待つ(By locator) throws Throwable {
        Wait<WebDriver> wait = new WebDriverWait(WebBrowser.INSTANCE, 3);
        wait.until(visibilityOfElementLocated(locator));
    }

    public void 消えるまで待つ(String elementId) throws Throwable {
        this.消えるまで待つ(By.id(elementId));
    }

    public void 消えるまで待つ(By locator) throws Throwable {
        Wait<WebDriver> wait = new WebDriverWait(WebBrowser.INSTANCE, 3);
        wait.until(invisibilityOfElementLocated(locator));
    }

    public void 要素_に_が表示されていること(By locator, String expected) throws Throwable {
        assertThat(WebBrowser.INSTANCE.findElement(locator).getText()).isEqualTo(expected);
    }

}
