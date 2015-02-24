package features.stepdefs;

import static org.fest.assertions.api.Assertions.assertThat;
import cucumber.api.DataTable;
import cucumber.api.java.ja.ならば;
import cucumber.api.java.ja.もし;
import cucumber.api.java.ja.前提;
import features.pages.admin.HeaderModule;
import features.pages.admin.PurchaseOrderConfirmationPage;
import features.pages.admin.PurchaseOrderPage;
import features.pages.admin.dailyMenu.NewPage;
import features.pages.admin.menuItem.EditPage;
import features.support.CucumberUtils;
import features.support.WebBrowser;
import models.LocalUser;
import org.joda.time.DateTime;
import securesocial.core.Registry;

import java.util.List;
import java.util.Map;

public class AdminStepDefs {

    @前提("^以下のユーザを登録する:$")
    public void 以下のユーザを登録する(DataTable productParams) throws Throwable {
        List<Map<String, String>> users = productParams.asMaps(String.class, String.class);

        for (Map<String, String>user : users) {
            LocalUser newUser = new LocalUser();
            newUser.email = user.get("メールアドレス");
            newUser.id = newUser.email;
            newUser.firstName = user.get("名");
            newUser.lastName = user.get("姓");
            newUser.password = Registry.hashers().get("bcrypt").get().hash(user.get("パスワード")).password();
            newUser.provider = "userpass";
            newUser.isAdmin = "管理者".equals(user.get("ロール"));
            newUser.save();
        }

        for (Map<String, String>user : users) {
            LocalUser registeredUser = LocalUser.find.byId(user.get("メールアドレス"));
            if (registeredUser == null) {
                throw new Exception("ユーザの作成に失敗しました");
            }
        }
    }

    @もし("^以下の商品を登録する:$")
    public void 以下の商品を登録する(DataTable productParams) throws Throwable {

        features.pages.admin.HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.menuItem.IndexPage indexPage = headerModule.showMenuItemsIndex();

        List<Map<String, String>> products = productParams.asMaps(String.class, String.class);

        for (Map<String, String>product : products) {
            EditPage editPage = indexPage.addProduct(product);

            editPage.setShopName(product.get("店名"));
            editPage.setRegisterNumber(product.get("レジ番号"));
            editPage.setCategory(product.get("カテゴリ"));
            editPage.setItemNumber(product.get("商品番号"));
            editPage.setName(product.get("商品名"));
            editPage.setFixedOnOrder(product.get("注文(定価)"));
            editPage.setDiscountOnOrder(product.get("注文(割引額)"));
            editPage.setFixedOnPurchaseExcTax(product.get("発注(税抜)"));
            editPage.setFixedOnPurchaseIncTax(product.get("発注(税込)"));
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

        DateTime menuDate = CucumberUtils.parseDate(menuParams.get("日付"));

        newPage.setWeek(menuDate);
        newPage.setDate(menuDate);
        newPage.setStatus(menuParams.get("ステータス"));

        int itemIndex = 1;
        do {
            String keyName = String.format("商品-%d", itemIndex);

            if (!menuParams.containsKey(keyName)) {
                break;
            }

            String[] itemNameParams = menuParams.get(keyName).split("　", 4);
            newPage.addItem(itemNameParams[0], itemNameParams[1], itemNameParams[2], itemNameParams[3]);

            itemIndex += 1;
        } while (true);
    }

    @ならば("^日付 \"(.*)\" のチェック表の総額が \"(.*)\" かつ、以下の内容であること:$")
    public void 日付_のチェック表が以下の内容であること(
            String menuDateStr,
            String totalPriceOnOrder,
            DataTable checkListExpected
    ) throws Throwable {

        DateTime menuDate = CucumberUtils.parseDate(menuDateStr);

        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.IndexPage indexPage = headerModule.showAdminIndex();
        features.pages.admin.ChecklistPage checklistPage = indexPage.showCheckList(menuDate);

        assertThat(checklistPage.getTotalPriceOnOrder()).isEqualTo(totalPriceOnOrder);

        List<Map<String, String>> actual = checklistPage.getList();
        checkListExpected.diff(actual);
    }

    @ならば("^日付 \"(.*)\" の注文確認シートにデータがないこと$")
    public void 日付_の注文確認シートにデータがないこと(String orderDateStr) throws Throwable {

        DateTime orderDate = CucumberUtils.parseDate(orderDateStr);
        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.IndexPage indexPage = headerModule.showAdminIndex();
        PurchaseOrderConfirmationPage purchaseOrderConfirmationPage = indexPage.showPurchaseOrderConfirmation(orderDate);

        List<Map<String, String>> actual = purchaseOrderConfirmationPage.getList(orderDate);
        assertThat(actual.size()).describedAs("リストのサイズ").isEqualTo(0);
    }

    @ならば("^日付 \"(.*)\" の注文確認シートが以下の内容であること:$")
    public void 日付_の注文確認シートが以下の内容であること(
            String orderDateStr,
            DataTable orderAggregatesExpected
    ) throws Throwable {

        DateTime orderDate = CucumberUtils.parseDate(orderDateStr);

        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.IndexPage indexPage = headerModule.showAdminIndex();
        PurchaseOrderConfirmationPage purchaseOrderConfirmationPage = indexPage.showPurchaseOrderConfirmation(orderDate);

        List<Map<String, String>> actual = purchaseOrderConfirmationPage.getList(orderDate);
        orderAggregatesExpected.diff(actual);
    }

    @ならば("^日付 \"(.*)\" の注文シートにデータがないこと$")
    public void 日付_の注文シートにデータがないこと(String orderDateStr) throws Throwable {

        DateTime orderDate = CucumberUtils.parseDate(orderDateStr);
        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.IndexPage indexPage = headerModule.showAdminIndex();
        PurchaseOrderPage purchaseOrderPage = indexPage.showPurchaseOrder(orderDate);

        List<Map<String, String>> actual = purchaseOrderPage.getList(orderDate);
        assertThat(actual.size()).describedAs("リストのサイズ").isEqualTo(0);
    }

    @ならば("^日付 \"(.*)\" の注文シートの総数量が \"(.*)\" 総額が \"(.*)\" 円かつ、内訳が以下の内容であること:$")
    public void 日付_の注文シートが以下の内容であること(
            String orderDateStr,
            String dayTotalNumOrders,
            String dayTotalFixedOnPurchaseIncTax,
            DataTable orderAggregatesExpected
    ) throws Throwable {

        DateTime orderDate = CucumberUtils.parseDate(orderDateStr);

        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.IndexPage indexPage = headerModule.showAdminIndex();
        PurchaseOrderPage purchaseOrderPage = indexPage.showPurchaseOrder(orderDate);

        assertThat(purchaseOrderPage.getDayTotalNumOrders(orderDate)).describedAs("総数量").isEqualTo(dayTotalNumOrders);
        assertThat(purchaseOrderPage.getDayTotalFixedOnPurchaseIncTax(orderDate)).describedAs("総額").isEqualTo(dayTotalFixedOnPurchaseIncTax);

        List<Map<String, String>> actual = purchaseOrderPage.getList(orderDate);
        orderAggregatesExpected.diff(actual);
    }

    @ならば("^ユーザ一覧が以下の内容であること:$")
    public void ユーザ一覧が以下の内容であること(DataTable usersExpected) throws Throwable {
        HeaderModule headerModule = new HeaderModule(WebBrowser.INSTANCE);
        features.pages.admin.user.IndexPage indexPage = headerModule.showUsersIndex();

        List<Map<String, String>> actual = indexPage.getList();
        usersExpected.diff(actual);
    }

}
