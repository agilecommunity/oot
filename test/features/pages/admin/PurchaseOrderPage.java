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

    public String getDayTotalNumOrders(DateTime orderDate) {
        By dayTotalRowLocator = By.xpath(String.format("tbody/tr[contains(@class, 'day-total')]", orderDate.toString("yyyyMMdd")));
        WebElement statusRow = this.getDetailTable(orderDate).findElement(dayTotalRowLocator);

        return statusRow.findElement(By.xpath("td[2]")).getText();
    }

    public String getDayTotalFixedOnPurchaseIncTax(DateTime orderDate) {
        By dayTotalRowLocator = By.xpath(String.format("tbody/tr[contains(@class, 'day-total')]", orderDate.toString("yyyyMMdd")));
        WebElement statusRow = this.getDetailTable(orderDate).findElement(dayTotalRowLocator);

        return statusRow.findElement(By.xpath("td[3]")).getText();
    }

    public List<Map<String, String>> getList(DateTime orderDate) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        try {
            By noDataLocator = By.xpath(String.format("//div[@id='purchase-order-%s']/div/p[contains(@class, 'no-data')]", orderDate.toString("yyyyMMdd")));
            this.base.findElement(noDataLocator);
            return list;
        } catch (NoSuchElementException ex) {
            // 次の処理にいくので、無視
        }

        By statusRowsLocator = By.xpath(String.format("tbody/tr[not(contains(@class, 'day-total'))]", orderDate.toString("yyyyMMdd")));
        List<WebElement> statusRows = this.getDetailTable(orderDate).findElements(statusRowsLocator);

        for (WebElement statusRow : statusRows) {
            Map<String, String> checkRow = new HashMap<String, String>();

            checkRow.put("レジ", statusRow.findElement(By.xpath("td[1]")).getText());
            checkRow.put("ショップ名", statusRow.findElement(By.xpath("td[2]")).getText());
            checkRow.put("No", statusRow.findElement(By.xpath("td[3]")).getText());
            checkRow.put("品名", statusRow.findElement(By.xpath("td[4]")).getText());
            checkRow.put("税抜", statusRow.findElement(By.xpath("td[5]")).getText());
            checkRow.put("税込", statusRow.findElement(By.xpath("td[6]")).getText());
            checkRow.put("数量", statusRow.findElement(By.xpath("td[7]")).getText());
            checkRow.put("金額", statusRow.findElement(By.xpath("td[8]")).getText());

            list.add(checkRow);
        }

        return list;
    }

    private WebElement getDetailTable(DateTime orderDate) {
        By locator = By.xpath(String.format("//table[@id='purchase-order-details-%s']", orderDate.toString("yyyyMMdd")));
        return this.base.findElement(locator);
    }
}
