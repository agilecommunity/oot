package features.stepdefs;

import cucumber.api.java.ja.もし;
import features.pages.SigninPage;
import features.pages.UserLinksModule;
import features.pages.dialog.ErrorDialogPage;
import features.pages.resetPassword.ResetPage;
import features.pages.resetPassword.StartResetPage;
import features.support.GlobalHooks;
import features.support.WebBrowser;
import models.LocalToken;

public class SigninStepDefs {

    @もし("^ユーザ \"(.*?)\" パスワード \"(.*?)\" でサインインする$")
    public void ユーザ_パスワード_でログインする(String userEmail, String userPassword) throws Throwable {

        WebBrowser.INSTANCE.get(String.format("http://localhost:%d/", GlobalHooks.PORT));

        SigninPage page = new SigninPage(WebBrowser.INSTANCE);
        page.サインイン(userEmail, userPassword);
    }

    @もし("^サインアウトする$")
    public void さいんあうとする() throws Throwable {

        UserLinksModule userLinksModule = new UserLinksModule(WebBrowser.INSTANCE);
        SigninPage signinPage = userLinksModule.signout();
    }

    @もし("^ユーザ \"(.*?)\" パスワード \"(.*?)\" でパスワードの初期化をする$")
    public void ユーザ_パスワード_でパスワードの初期化をする(String userEmail, String userPassword) throws Throwable {

        WebBrowser.INSTANCE.get(String.format("http://localhost:%d/", GlobalHooks.PORT));

        SigninPage signinPage = new SigninPage(WebBrowser.INSTANCE);
        StartResetPage startResetPage = signinPage.パスワードのリセット();

        startResetPage.setEmail(userEmail);
        startResetPage.メールの送付();

        LocalToken token = LocalToken.find.where().eq("email", userEmail).findUnique();
        WebBrowser.INSTANCE.get(String.format("http://localhost:%d/#/reset/%s", GlobalHooks.PORT, token.uuid));

        ResetPage resetPage = new ResetPage(WebBrowser.INSTANCE);
        resetPage.setPassword(userPassword);
        resetPage.パスワードのリセット();
    }

    @もし("^ユーザ \"(.*?)\" パスワード \"(.*?)\" でサインインできること$")
    public void ユーザ_パスワード_でサインインできること(String userEmail, String userPassword) throws Throwable {
        SigninStepDefs stepDefs = new SigninStepDefs();
        stepDefs.ユーザ_パスワード_でログインする(userEmail, userPassword);
    }

    @もし("^ユーザ \"(.*?)\" パスワード \"(.*?)\" でサインインできないこと$")
    public void ユーザ_パスワード_でサインインできないこと(String userEmail, String userPassword) throws Throwable {
        WebBrowser.INSTANCE.get(String.format("http://localhost:%d/", GlobalHooks.PORT));

        SigninPage page = new SigninPage(WebBrowser.INSTANCE);
        ErrorDialogPage dialogPage = page.サインイン_ExpectingFailure(userEmail, userPassword);
        dialogPage.close();
    }

}