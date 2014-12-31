package features.stepdefs;

import cucumber.api.DataTable;
import cucumber.api.java.ja.もし;
import features.pages.admin.HeaderModule;
import features.pages.admin.menuItem.EditPage;
import features.pages.admin.menuItem.IndexPage;
import features.support.WebBrowser;

import java.util.List;
import java.util.Map;

public class AdminStepDefs {

    @もし("^以下の商品を登録する:$")
    public void 以下の商品を登録する(DataTable productParams) throws Throwable {

        features.pages.admin.HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.menuItem.IndexPage indexPage = headerModule.商品一覧画面表示();

        List<Map<String, String>> products = productParams.asMaps(String.class, String.class);

        for (Map<String, String>product : products) {
            EditPage editPage = indexPage.商品追加(product);

            editPage.setShopName(product.get("店名"));
            editPage.setCategory(product.get("カテゴリ"));
            editPage.setName(product.get("商品名"));
            editPage.setPriceOnOrder(product.get("注文価格"));
            editPage.setCode(product.get("商品コード"));
            editPage.setStatus(product.get("ステータス"));
            editPage.save();

            Thread.sleep(1000); //FIXME 何をもって官僚と見做すのが良いか? 誰が完了したとみなすのか?
        }
    }
}
