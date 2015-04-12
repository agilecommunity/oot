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

    /**
     * 注文をする
     * @param orderDate
     * @param shopName
     * @param itemName
     * @param priceOnOrder  価格 単位を含めること (ex. "100円")
     * @throws Throwable
     */
    public void order(DateTime orderDate, String shopName, String itemName, String priceOnOrder) throws Throwable {

        By dayBaseLocator = By.id(String.format("day-%s", orderDate.toString("yyyyMMdd")));
        WebElement dayBase = base.findElement(dayBaseLocator);

        By itemLocator = By.xpath(String.format("//div[contains(@class, 'menu-item-list')]//div[contains(@class,'menu-item')]//div[@class='caption' and div[contains(@class,'shop-name') and text()='%s'] and div[contains(@class, 'food-name') and text()='%s ' and span/span[text()='%s']]]", shopName, itemName, priceOnOrder));
        SeleniumUtils.waitAndClick(this.driver, itemLocator);

        By orderdItemLocator = By.xpath(String.format("//div[@id='day-%s']//div[contains(@class, 'menu-item-list')]//div[contains(@class,'menu-item') and contains(@class, 'ordered')]//div[@class='caption' and div[contains(@class,'shop-name') and text()='%s'] and div[contains(@class, 'food-name') and text()='%s ' and span/span[text()='%s']]]", orderDate.toString("yyyyMMdd"), shopName, itemName, priceOnOrder));
        SeleniumUtils.waitForVisible(this.driver, orderdItemLocator);

    }

    public String getGatheringStatus(DateTime orderDate) {

        By dayBaseLocator = By.id(String.format("day-%s", orderDate.toString("yyyyMMdd")));
        WebElement dayBase = base.findElement(dayBaseLocator);

        By itemLocator = By.cssSelector("div.day-header div.gathering-status");
        return dayBase.findElement(itemLocator).getText().replace("\n", " ");
    }

}
