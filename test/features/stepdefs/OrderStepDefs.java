package features.stepdefs;

import static org.fest.assertions.Assertions.*;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cucumber.api.DataTable;
import cucumber.api.Transform;
import cucumber.api.java.ja.ならば;
import cucumber.api.java.ja.もし;
import features.stepdefs.common.WebStepDefs;
import features.support.JodaTimeConverter;
import features.support.WebBrowser;

public class OrderStepDefs {

    WebStepDefs webStepDefs = new WebStepDefs();

    @もし("^日付 \"(.*?)\" の注文をする:$")
    public void 日付_の注文をする(@Transform(JodaTimeConverter.class) LocalDate orderDate, DataTable orders) throws Throwable {

        List<Map<String, String>> orderList = orders.asMaps(String.class, String.class);

        for(Map<String, String> order : orderList) {
            WebElement elemOrderItem = this.findOrderItem(orderDate, order.get("店名"), order.get("品名"));
            elemOrderItem.click();
        }
        webStepDefs.ミリ秒待つ(1000L); // FIXME: ダサい(ホントは通信が完了まで待ちたい)
    }

    @ならば("^日付 \"(.*?)\" の合計金額が \"(.*?)\" となっていること$")
    public void 日付_の合計金額が_となっていること(@Transform(JodaTimeConverter.class) LocalDate orderDate, String priceStr) throws Throwable {

        WebElement elemSmallPrice = this.findSmallPrice(orderDate);

        assertThat(elemSmallPrice.getText()).isEqualTo(priceStr);
    }

    private WebElement findSmallPrice(LocalDate orderDate) throws Throwable {
        WebElement component = this.findOrderComponent(orderDate);

        return component.findElement(By.xpath("//div[contains(@class, 'small-total')]/span[contains(@class, 'price')]"));
    }

    private WebElement findOrderItem(LocalDate orderDate, String shopName, String itemName) throws Throwable {
        WebElement component = this.findOrderComponent(orderDate);

        By locator = By.xpath(String.format("//div[contains(@class, 'shop-name') and text()='%s']/parent::node()", shopName));

        return component.findElement(locator);
    }

    private WebElement findOrderComponent(LocalDate orderDate) throws Throwable {
        return WebBrowser.INSTANCE.findElement(By.id(String.format("day-%s", orderDate.toString("yyyyMMdd"))));
    }
}
