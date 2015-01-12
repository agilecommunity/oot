package features.pages;

import com.thoughtworks.selenium.Selenium;
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

public class OrderPage {

    private static Logger logger = LoggerFactory.getLogger(OrderPage.class);

    private WebDriver driver;

    public OrderPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.order"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how=How.CSS, using="div.content.order")
    private WebElement base;

    public void order(DateTime orderDate, String shopName, String itemName) throws Throwable {

        By dayBaseLocator = By.id(String.format("day-%s", orderDate.toString("yyyyMMdd")));
        WebElement dayBase = base.findElement(dayBaseLocator);

        By itemLocator = By.xpath(String.format("div[@class='choose-list']//div[contains(@class,'menu menu-item')]/div[div[contains(@class,'shop-name') and text()='%s'] and div[contains(@class, 'food-name') and text()='%s']]", shopName, itemName));
        WebElement item = dayBase.findElement(itemLocator);
        item.click();

        By orderdItemLocator = By.xpath(String.format("//div[@id='day-%s']/div[@class='choose-list']//div[contains(@class,'menu menu-item') and contains(@class, 'ordered') and div[div[contains(@class,'shop-name') and text()='%s'] and div[contains(@class, 'food-name') and text()='%s']]]", orderDate.toString("yyyyMMdd"), shopName, itemName));
        SeleniumUtils.waitForVisible(this.driver, orderdItemLocator);

    }

}
