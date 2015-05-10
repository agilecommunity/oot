package features.pages;

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

public class SelectNumOrdersPage {

    private static Logger logger = LoggerFactory.getLogger(SelectNumOrdersPage.class);

    private static final String BASE_XPATH = "//div[contains(@class,'modal-dialog') and div[@class='modal-content']/div[contains(@class, 'modal-header')]/h4[text()='注文数指定']]";

    private WebDriver driver;

    public SelectNumOrdersPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.xpath(BASE_XPATH));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.xpath(BASE_XPATH))), this);
        this.driver = driver;
    }

    @FindBy(how= How.XPATH, using=BASE_XPATH)
    WebElement base;

    public void select(Integer numOrders) throws Throwable {
        By locator = By.xpath(String.format("//button[contains(@class, 'btn btn-primary') and text()='%d個']", numOrders));
        base.findElement(locator).click();

        SeleniumUtils.waitForInvisible(this.driver, locator);
    }
}
