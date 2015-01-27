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

    @FindBy(how= How.CSS, using="button.navbar-btn.select-shops")
    private WebElement showSelectShop;

    public SelectShopPage selectShop() throws Throwable {
        this.showSelectShop.click();
        return new SelectShopPage(this.driver);
    }

    public void select(String itemName, String priceOnOrder) {
        By itemLocator = By.xpath(String.format("//div[contains(@class, 'menu-item-sm') and div[@class='caption']/div[contains(@class, 'food-name') and text()='%s %s']]", itemName, priceOnOrder));
        WebElement button = this.base.findElement(itemLocator);
        button.click();

        SeleniumUtils.waitForInvisible(this.driver, By.xpath(baseXPath));
    }
}
