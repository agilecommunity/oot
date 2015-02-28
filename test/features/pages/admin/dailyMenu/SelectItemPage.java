package features.pages.admin.dailyMenu;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.thoughtworks.selenium.Selenium;
import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
        SeleniumUtils.waitAndClick(this.driver, itemLocator);

        return new SelectShopPage(this.driver);
    }

    public void select(String itemName, String reducedOnOrder) throws Throwable {
        By itemLocator = By.xpath(String.format("//div[contains(@class, 'menu-item-sm') and div[@class='caption']/div[contains(@class, 'food-name') and text()='%s ' and span/span[text()='%s']]]", itemName, reducedOnOrder));
        SeleniumUtils.waitAndClick(this.driver, itemLocator);

        try {
            if (this.driver.findElement(By.xpath(baseXPath)) != null) {
                SeleniumUtils.waitForInvisible(this.driver, By.xpath(baseXPath));
            }
        } catch (NoSuchElementException ex) {
            // なくなっている場合は何もしなくてよい
        }
    }
}
