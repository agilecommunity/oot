package features.pages.admin;

import features.support.SeleniumUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseOrderPage {
    private static Logger logger = LoggerFactory.getLogger(PurchaseOrderConfirmationPage.class);

    private WebDriver driver;

    public PurchaseOrderPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-purchase-order"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how= How.CSS, using="div.content.admin-purchase-order")
    private WebElement base;

    public List<Map<String, String>> getList(DateTime orderDate) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        try {
            By noDataLocator = By.xpath(String.format("//div[@id='purchase-order-%s']/div/p[contains(@class, 'no-data')]", orderDate.toString("yyyyMMdd")));
            this.base.findElement(noDataLocator);
            return list;
        } catch (NoSuchElementException ex) {
            // 次の処理にいくので、無視
        }

        By productRowsLocator = By.xpath(String.format("tbody/tr[(contains(@class, 'product'))]"));
        List<WebElement> productRows = this.getDetailTable(orderDate).findElements(productRowsLocator);

        for (WebElement productRow : productRows) {
            Map<String, String> item = new HashMap<String, String>();

            item.put("レジ", productRow.findElement(By.xpath("td[1]")).getText());
            item.put("ショップ名", productRow.findElement(By.xpath("td[2]")).getText());
            item.put("No", productRow.findElement(By.xpath("td[3]")).getText());
            item.put("品名", productRow.findElement(By.xpath("td[4]")).getText());
            item.put("税抜", productRow.findElement(By.xpath("td[5]")).getText());
            item.put("税込", productRow.findElement(By.xpath("td[6]")).getText());
            item.put("数量", productRow.findElement(By.xpath("td[7]")).getText());
            item.put("金額", productRow.findElement(By.xpath("td[8]")).getText());

            list.add(item);
        }

        /* 小計 */
        {
            By rowLocator = By.xpath(String.format("tbody/tr[(contains(@class, 'day-sub-total'))]"));
            WebElement row = this.getDetailTable(orderDate).findElement(rowLocator);

            Map<String, String> item = new HashMap<String, String>();
            item.put("レジ", "");
            item.put("ショップ名", "");
            item.put("No", "");
            item.put("品名", "小計");
            item.put("税抜", "");
            item.put("税込", "");
            item.put("数量", row.findElement(By.xpath("td[2]")).getText());
            item.put("金額", row.findElement(By.xpath("td[3]")).getText());

            list.add(item);
        }

        /* 追加 */
        {
            By rowsLocator = By.xpath(String.format("tbody/tr[(contains(@class, 'additional-item'))]"));
            List<WebElement> rows  = this.getDetailTable(orderDate).findElements(rowsLocator);

            for (WebElement row : rows) {
                Map<String, String> item = new HashMap<String, String>();

                item.put("レジ", "");
                item.put("ショップ名", "");
                item.put("No", "");
                item.put("品名", row.findElement(By.xpath("td[1]")).getText());
                item.put("税抜", "");
                item.put("税込", "");
                item.put("数量", row.findElement(By.xpath("td[3]")).getText());
                item.put("金額", row.findElement(By.xpath("td[4]")).getText());

                list.add(item);
            }
        }

        /* 合計 */
        {
            By rowLocator = By.xpath(String.format("tbody/tr[(contains(@class, 'day-total'))]"));
            WebElement row = this.getDetailTable(orderDate).findElement(rowLocator);

            Map<String, String> item = new HashMap<String, String>();
            item.put("レジ", "");
            item.put("ショップ名", "");
            item.put("No", "");
            item.put("品名", "合計");
            item.put("税抜", "");
            item.put("税込", "");
            item.put("数量", row.findElement(By.xpath("td[2]")).getText());
            item.put("金額", row.findElement(By.xpath("td[3]")).getText());

            list.add(item);
        }

        return list;
    }

    private WebElement getDetailTable(DateTime orderDate) {
        By locator = By.xpath(String.format("//table[@id='purchase-order-details-%s']", orderDate.toString("yyyyMMdd")));
        return this.base.findElement(locator);
    }
}
