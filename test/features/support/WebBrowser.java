package features.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverEngine;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;

import java.io.File;

public class WebBrowser {

    public static WebDriver INSTANCE = null;

    public static void setUp() {
        if (INSTANCE == null) {
            // なぜかIE11だと上手く動作するので、こちらを利用
            // FirefoxDriverだとログインに失敗してしまう (セッションIDがCookieに保存されないようだ)
            System.setProperty("webdriver.ie.driver", "test\\features\\drivers\\IEDriverServer.exe");
            InternetExplorerDriverService service =
                    new InternetExplorerDriverService.Builder()
                            .withEngineImplementation(InternetExplorerDriverEngine.LEGACY)
                            .usingAnyFreePort()
                            .withLogLevel(InternetExplorerDriverLogLevel.TRACE)
                            .withLogFile(new File("logs/iedriver.log"))
                            .build();
            INSTANCE = new InternetExplorerDriver(service);
            //INSTANCE = new FirefoxDriver();
        }
    }

    public static void tearDown() {
        if (INSTANCE != null) {
            INSTANCE.quit();
        }
    }
}
