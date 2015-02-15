package features.pages.admin.dailyMenu;

import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectItemPage {

    private static Logger logger = LoggerFactory.getLogger(SelectItemPage.class);

    private static final String baseXPath = "//div[contains(@class,'modal-dialog') and div[@class='modal-content']/div[contains(@class, 'modal-header')]/h4[text()='商品選択']]";

    private WebDriver driver;

    public SelectItemPage(WebDriver driver) throws Throwable {

        By baseLocator = By.xpath(baseXPath);
        SeleniumUtils.waitForVisible(driver, baseLocator);

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(baseLocator)), this);
        this.driver = driver;
    }

    @FindBy(how= How.XPATH, using=baseXPath)
    private WebElement base;

    public SelectShopPage selectShop() throws Throwable {

        By itemLocator = By.cssSelector("button.navbar-btn.select-shops");
        SeleniumUtils.waitForVisible(this.driver, itemLocator);

        WebElement button = this.base.findElement(itemLocator);
        button.click();

        return new SelectShopPage(this.driver);
    }

    public void select(String itemName, String reducedOnOrder) throws Throwable {
        By itemLocator = By.xpath(String.format("//div[contains(@class, 'menu-item-sm') and div[@class='caption']/div[contains(@class, 'food-name') and text()='%s ' and span/span[text()='%s']]]", itemName, reducedOnOrder));

        SeleniumUtils.waitForVisible(this.driver, itemLocator);

        WebElement button = this.base.findElement(itemLocator);
        button.click();

        SeleniumUtils.waitForInvisible(this.driver, By.xpath(baseXPath));
    }
}
