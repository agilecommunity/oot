package features.stepdefs;

import cucumber.api.java.ja.もし;
import features.stepdefs.common.WebStepDefs;

public class LoginStepDefs {

    private WebStepDefs webStepDefs = new WebStepDefs();

    @もし("^ユーザ \"(.*?)\" パスワード \"(.*?)\" でログインする$")
    public void ユーザ_パスワード_でログインする(String userEmail, String userPassword) throws Throwable {

        webStepDefs.アクセスする("http://localhost:3333");

        webStepDefs.現れるまで待つ("user.email");

        webStepDefs.フィールド_に_を入力する("user.email", userEmail);
        webStepDefs.フィールド_に_を入力する("user.password", userPassword);

        webStepDefs.ボタンをクリックする("signin");

        webStepDefs.消えるまで待つ("user.email");
    }
}
