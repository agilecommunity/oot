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

public class SelectShopPage {

    private static Logger logger = LoggerFactory.getLogger(SelectShopPage.class);

    private static final String baseXPath = "//div[contains(@class,'modal-dialog') and div[@class='modal-content']/div[contains(@class, 'modal-header')]/h4[text()='店舗選択']]";

    private WebDriver driver;

    public SelectShopPage(WebDriver driver) throws Throwable {

        By baseLocator = By.xpath(baseXPath);
        SeleniumUtils.waitForVisible(driver, baseLocator);

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(baseLocator)), this);
        this.driver = driver;
    }

    @FindBy(how=How.XPATH, using=baseXPath)
    private WebElement base;

    public void select(String value) {
        By shopLocator = By.xpath(String.format("//button[text()='%s']", value));
        WebElement button = this.base.findElement(shopLocator);
        button.click();

        SeleniumUtils.waitForInvisible(this.driver, By.xpath(baseXPath));
    }
}
