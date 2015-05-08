package features.pages.admin;

import features.pages.admin.checklist.DailyPage;
import features.support.SeleniumUtils;
import org.apache.commons.collections.map.HashedMap;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
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

public class IndexPage {

    private static Logger logger = LoggerFactory.getLogger(IndexPage.class);

    private WebDriver driver;

    public IndexPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-index"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how=How.CSS, using="div.content.admin-index")
    public WebElement base;

    public Map<String, String> getStatus(DateTime menuDate) throws Throwable {
        Map<String, String> status = new HashMap<String, String>();

        DateTime startDate = menuDate.withDayOfWeek(1);

        logger.debug("#getStatus menuDate: {} startDate: {}", menuDate, startDate);

        By statusTableLocator = By.cssSelector(String.format("div#dashboard-panel-%s div.panel-body table.table-day-status", startDate.toString("yyyyMMdd")));
        WebElement statusTable = base.findElement(statusTableLocator);

        Integer colBaseIndex = menuDate.getDayOfWeek() - startDate.getDayOfWeek();
        Integer oneColIndex = 1 + colBaseIndex;
        Integer twoLIndex = 1 + (colBaseIndex * 2);
        Integer twoRIndex = 2 + (colBaseIndex * 2);

        status.put("ステータス", this.getText(statusTable, By.xpath(String.format("thead/tr/th[%d]/label[contains(@class,'label-menu-status')]", oneColIndex + 1))));
        status.put("注文人数", this.getText(statusTable, By.xpath(String.format("tbody/tr[1]/td[%d]", oneColIndex))));
        status.put("注文数", this.getText(statusTable, By.xpath(String.format("tbody/tr[2]/td[%d]", twoLIndex))));
        status.put("注文数 弁当", this.getText(statusTable, By.xpath(String.format("tbody/tr[2]/td[%d]", twoRIndex))));
        status.put("注文数 サイドメニュー", statusTable.findElement(By.xpath(String.format("tbody/tr[3]/td[%d]", oneColIndex))).getText().trim());
        status.put("注文(定価)", this.getText(statusTable, By.xpath(String.format("tbody/tr[4]/td[%d]", twoLIndex))));
        status.put("注文(定価) 弁当", this.getText(statusTable, By.xpath(String.format("tbody/tr[4]/td[%d]", twoRIndex))));
        status.put("注文(定価) サイドメニュー", statusTable.findElement(By.xpath(String.format("tbody/tr[5]/td[%d]", oneColIndex))).getText().trim());
        status.put("ギャザリング 目標", this.getText(statusTable, By.xpath(String.format("tbody/tr[6]/th[1]/span"))).replace("目標: ", ""));
        status.put("ギャザリング ステータス", this.getText(statusTable, By.xpath(String.format("tbody/tr[6]/td[%d]", oneColIndex))));

        return status;
    }

    public DailyPage showCheckList(DateTime menuDate) throws Throwable {
        By buttonLocator = By.id(String.format("show-checklist-daily-%s", menuDate.toString("yyyyMMdd")));
        SeleniumUtils.waitAndClick(this.driver, buttonLocator);

        return new DailyPage(this.driver);
    }

    public features.pages.admin.PurchaseOrderConfirmationPage showPurchaseOrderConfirmation(DateTime orderDate) throws Throwable {
        DateTime startDayOfWeek = orderDate.withDayOfWeek(1);

        By buttonLocator = By.id(String.format("show-purchase-order-confirmation-%s", startDayOfWeek.toString("yyyyMMdd")));
        SeleniumUtils.waitAndClick(this.driver, buttonLocator);

        return new features.pages.admin.PurchaseOrderConfirmationPage(this.driver);
    }

    public PurchaseOrderPage showPurchaseOrder(DateTime orderDate) throws Throwable {
        DateTime startDayOfWeek = orderDate.withDayOfWeek(1);

        By buttonLocator = By.id(String.format("show-purchase-order-%s", startDayOfWeek.toString("yyyyMMdd")));
        SeleniumUtils.waitAndClick(this.driver, buttonLocator);

        return new features.pages.admin.PurchaseOrderPage(this.driver);
    }

    public CashBookPage showCashBook(DateTime targetDate) throws Throwable {
        DateTime startDayOfWeek = targetDate.withDayOfWeek(1);

        By buttonLocator = By.id(String.format("show-cash-book-%s", startDayOfWeek.toString("yyyyMMdd")));
        SeleniumUtils.waitAndClick(this.driver, buttonLocator);

        return new features.pages.admin.CashBookPage(this.driver);
    }

    private String getText(WebElement base, By locator) {
        String text = base.findElement(locator).getText();

        if (text == null) {
            return "";
        }

        return text.trim();
    }
}
