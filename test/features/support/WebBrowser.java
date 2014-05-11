package features.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class WebBrowser {

    public static WebDriver INSTANCE = null;

    public static void setUp() {
        if (INSTANCE == null) {
            // なぜかIE11だと上手く動作するので、こちらを利用
            // FirefoxDriverだとログインに失敗してしまう (セッションIDがCookieに保存されないようだ)
            System.setProperty("webdriver.ie.driver", "test\\features\\drivers\\IEDriverServer.exe");
            INSTANCE = new InternetExplorerDriver();
        }
    }

    public static void tearDown() {
        if (INSTANCE != null) {
            INSTANCE.quit();
        }
    }
}
