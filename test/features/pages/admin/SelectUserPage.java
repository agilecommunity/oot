package features.pages.admin;

import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectUserPage {

    private static Logger logger = LoggerFactory.getLogger(SelectUserPage.class);

    private static final String BASE_XPATH = "//div[contains(@class,'modal-dialog') and div[@class='modal-content']/div[contains(@class, 'modal-header')]/h4[text()='ユーザ選択']]";

    private WebDriver driver;

    public SelectUserPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.xpath(BASE_XPATH));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.xpath(BASE_XPATH))), this);
        this.driver = driver;
    }

    @FindBy(how=How.XPATH, using=BASE_XPATH)
    WebElement base;

    /**
     * ユーザを選択する
     *
     * @param fullName "{{姓}} {{名}}"のフォーマットで指定
     */
    public void select(String fullName) throws Throwable {
        By locator = By.xpath(String.format("//button[text()='%s']", fullName));
        WebElement button = base.findElement(locator);

        button.click();

        SeleniumUtils.waitForInvisible(this.driver, locator);
    }
}
