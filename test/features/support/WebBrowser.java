package features.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverEngine;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.ie.InternetExplorerDriverService;

import java.io.File;
import java.util.logging.Level;

public class WebBrowser {

    public static WebDriver INSTANCE = null;

    public static void setUp() {
        if (INSTANCE == null) {

            String driverType = System.getProperty("selenium.driver", "ie");

            if ("firefox".equals(driverType)) {
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("webdriver.log.driver", "ALL");
                profile.setPreference("webdriver.log.file", new File("logs/webdriver-firefox.log").getAbsolutePath());
                INSTANCE = new FirefoxDriver(profile);
            } else {
                System.setProperty("webdriver.ie.driver", "test\\features\\drivers\\IEDriverServer.exe");
                InternetExplorerDriverService service =
                        new InternetExplorerDriverService.Builder()
                                .withEngineImplementation(InternetExplorerDriverEngine.LEGACY)
                                .usingAnyFreePort()
                                .withLogLevel(InternetExplorerDriverLogLevel.TRACE)
                                .withLogFile(new File("logs/webdriver-ie.log"))
                                .build();
                INSTANCE = new InternetExplorerDriver(service);
            }
        }
    }

    public static void tearDown() {
        if (INSTANCE != null) {
            INSTANCE.quit();
        }
    }
}
