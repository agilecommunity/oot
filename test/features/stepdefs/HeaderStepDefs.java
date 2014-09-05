package features.stepdefs;

import org.openqa.selenium.By;

import cucumber.api.java.ja.ならば;
import features.stepdefs.common.WebStepDefs;

public class HeaderStepDefs {

    WebStepDefs webStepDefs = new WebStepDefs();

    @ならば("^タイトルに \"(.*?)\" と表示されていること$")
    public void タイトルに_と表示されていること(String titleName) throws Throwable {
        webStepDefs.要素_に_が表示されていること(By.xpath("//div[contains(@class, 'page-header')]/h1"), titleName);
    }

    @ならば("^ユーザ情報に \"(.*?)\" と表示されていること$")
    public void ユーザ情報に_と表示されていること(String userInfo) throws Throwable {
        webStepDefs.要素_に_が表示されていること(By.xpath("//div[contains(@class, 'user-info')]"), userInfo);
    }
}
