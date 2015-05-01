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

public class UserLinksModule {

    private static Logger logger = LoggerFactory.getLogger(UserLinksModule.class);

    private WebDriver driver;

    public UserLinksModule(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("#user-links"));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.cssSelector("#user-links"))), this);
        this.driver = driver;
    }

    @FindBy(how= How.CLASS_NAME, using="full-name")
    private WebElement userFullName;

    @FindBy(how= How.CLASS_NAME, using="btn-signout")
    private WebElement signout;

    public String getFullName() {
        return this.userFullName.getText();
    }

    public SigninPage signout() {
        this.signout.click();
        return new SigninPage(this.driver);
    }
}
