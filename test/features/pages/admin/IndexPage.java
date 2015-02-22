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
        WebElement button = base.findElement(buttonLocator);
        button.click();

        return new features.pages.admin.ChecklistPage(this.driver);
    }

    public features.pages.admin.PurchaseOrderConfirmationPage showPurchaseOrderConfirmation(DateTime menuDate) throws Throwable {
        DateTime startDayOfWeek = menuDate.withDayOfWeek(1);

        By buttonLocator = By.id(String.format("show-purchase-order-confirmation-%s", startDayOfWeek.toString("yyyyMMdd")));
        WebElement button = base.findElement(buttonLocator);
        button.click();

        return new features.pages.admin.PurchaseOrderConfirmationPage(this.driver);
    }

}
