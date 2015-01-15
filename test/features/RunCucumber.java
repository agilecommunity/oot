package features;

import org.junit.AfterClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import features.support.WebBrowser;

@RunWith(Cucumber.class)
@CucumberOptions(
      glue = {"features.stepdefs", "features.support"}
    , features = {"test/features/scenarios"}
    , format = {"pretty"}
)
public class RunCucumber {

    @AfterClass
    public static void afterClass() {
        // Cucumberのテストがすべて終わった後、ブラウザを終了させる
        WebBrowser.tearDown();
    }
}
