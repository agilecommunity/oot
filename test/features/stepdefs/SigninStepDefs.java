package features.stepdefs;

import cucumber.api.java.ja.もし;
import features.pages.SigninPage;
import features.stepdefs.common.WebStepDefs;
import features.support.GlobalHooks;
import features.support.WebBrowser;
import org.fest.assertions.ThrowableAssert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SigninStepDefs {

    private WebStepDefs webStepDefs = new WebStepDefs();

    @もし("^ユーザ \"(.*?)\" パスワード \"(.*?)\" でサインインする$")
    public void ユーザ_パスワード_でログインする(String userEmail, String userPassword) throws Throwable {

        WebBrowser.INSTANCE.get(String.format("http://localhost:%d/", GlobalHooks.PORT));

        SigninPage page = new SigninPage(WebBrowser.INSTANCE);
        page.サインイン(userEmail, userPassword);
    }

    @もし("^サインアウトする$")
    public void さいんあうとする() throws Throwable {
        // まだサインアウトが実装されてないので、ひとまずルート画面に戻る
        WebBrowser.INSTANCE.get(String.format("http://localhost:%d/", GlobalHooks.PORT));
    }
}
