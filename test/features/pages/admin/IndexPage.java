package features.pages.admin;

import features.support.SeleniumUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public features.pages.admin.ChecklistPage showCheckList(DateTime menuDate) throws Throwable {
        By buttonLocator = By.id(String.format("show-checklist-%s", menuDate.toString("yyyyMMdd")));
        SeleniumUtils.waitAndClick(this.driver, buttonLocator);

        return new features.pages.admin.ChecklistPage(this.driver);
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
}
