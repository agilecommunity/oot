package features.stepdefs;

import cucumber.api.java.ja.もし;
import features.pages.SigninPage;
import features.support.GlobalHooks;
import features.support.WebBrowser;

public class SigninStepDefs {

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
