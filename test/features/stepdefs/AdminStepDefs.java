package features.stepdefs;

import cucumber.api.DataTable;
import cucumber.api.java.ja.もし;
import features.pages.admin.HeaderModule;
import features.pages.admin.dailyMenu.NewPage;
import features.pages.admin.dailyMenu.SelectItemPage;
import features.pages.admin.dailyMenu.SelectShopPage;
import features.pages.admin.menuItem.EditPage;
import features.support.SeleniumUtils;
import features.support.WebBrowser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

public class AdminStepDefs {

    @もし("^以下の商品を登録する:$")
    public void 以下の商品を登録する(DataTable productParams) throws Throwable {

        features.pages.admin.HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.menuItem.IndexPage indexPage = headerModule.showMenuItemsIndex();

        List<Map<String, String>> products = productParams.asMaps(String.class, String.class);

        for (Map<String, String>product : products) {
            EditPage editPage = indexPage.addProduct(product);

            editPage.setShopName(product.get("店名"));
            editPage.setCategory(product.get("カテゴリ"));
            editPage.setName(product.get("商品名"));
            editPage.setPriceOnOrder(product.get("注文価格"));
            editPage.setCode(product.get("商品コード"));
            editPage.setStatus(product.get("ステータス"));
            editPage.save();

            Thread.sleep(1000); //FIXME 何をもって完了と見做すのが良いか? 誰が完了したとみなすのか?
        }
    }

    @もし("^以下の内容のメニューを作成する:$")
    public void 以下の内容のメニューを作成する(Map<String, String> menuParams) throws Throwable {

        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        NewPage newPage = headerModule.showOrderMenuNew();

        DateTime menuDate = DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(menuParams.get("日時"));
        newPage.setWeek(menuDate);
        newPage.setDate(menuDate);
        newPage.setStatus(menuParams.get("ステータス"));

        int itemIndex = 1;
        do {
            String keyName = String.format("商品-%d", itemIndex);

            if (!menuParams.containsKey(keyName)) {
                break;
            }

            String[] itemNameParams = menuParams.get(keyName).split("　", 2);
            newPage.addItem(itemNameParams[0], itemNameParams[1]);

            itemIndex += 1;
        } while (true);
    }
}
