package features.stepdefs;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cucumber.api.DataTable;
import cucumber.api.java.ja.ならば;
import features.support.WebBrowser;

public class CheckListStepDefs {

    @ならば("^日付 \"(.*?)\" のチェックリストが以下であること:$")
    public void 日付_のチェックリストが以下であること(String dateStr, DataTable orders) throws Throwable {

        List<ArrayList<String>> actual = new ArrayList<ArrayList<String>>();

        // チェックリスト(Table)を取得する
        WebElement elemCheckList = WebBrowser.INSTANCE.findElement(By.id("checklist"));

        // ヘッダ部を作る
        ArrayList<String> actualHeader = new ArrayList<String>();
        WebElement elemHeader = elemCheckList.findElement(By.tagName("thead"));
        List<WebElement> elemShopNames = elemHeader.findElements(By.className("shop-name"));
        List<WebElement> elemItemNames = elemHeader.findElements(By.className("item-name"));
        List<WebElement> elemPrices = elemHeader.findElements(By.className("price"));

        actualHeader.add(elemHeader.findElement(By.className("check-status")).getText());
        actualHeader.add(elemHeader.findElement(By.className("user-name")).getText());
        for(int index=0; index<elemShopNames.size(); index++) {
            StringBuilder values = new StringBuilder();
            values.append(elemShopNames.get(index).getText());
            values.append(" ");
            values.append(elemItemNames.get(index).getText());
            values.append(" ");
            values.append(elemPrices.get(index).getText());

            actualHeader.add(values.toString());
        }

        actual.add(actualHeader);

        // Body部を作る
        List<WebElement> elemBodyRows = elemCheckList.findElements(By.xpath("tbody/tr"));

        for(WebElement elemBodyRow : elemBodyRows) {
            ArrayList<String> actualBodyRow = new ArrayList<String>();

            for(WebElement elemCell : elemBodyRow.findElements(By.tagName("td"))) {
                actualBodyRow.add(elemCell.getText());
            }

            actual.add(actualBodyRow);
        }

        // 期待結果と実際の値を比較する
        orders.diff(actual);
    }
}
