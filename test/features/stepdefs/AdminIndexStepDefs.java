package features.stepdefs;

import org.joda.time.LocalDate;
import org.openqa.selenium.By;

import cucumber.api.Transform;
import cucumber.api.java.ja.もし;
import features.stepdefs.common.WebStepDefs;
import features.support.JodaTimeConverter;

public class AdminIndexStepDefs {

    WebStepDefs webStepDefs = new WebStepDefs();

    @もし("^日付 \"(.*?)\" のチェックリストを表示する$")
    public void 日付_のチェックリストを表示する(@Transform(JodaTimeConverter.class)LocalDate orderDate) throws Throwable {

        webStepDefs.ボタンをクリックする(By.xpath(String.format("//div[@id='day-%s']//button[text() = 'チェック表']", orderDate.toString("yyyyMMdd"))));

        webStepDefs.消えるまで待つ(By.xpath("//button[text() = 'チェック表']"));
    }
}
