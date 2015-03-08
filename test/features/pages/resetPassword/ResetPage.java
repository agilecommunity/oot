package features.pages.resetPassword;

import features.pages.SigninPage;
import features.pages.dialog.NotifyDialogPage;
import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetPage {

    private static Logger logger = LoggerFactory.getLogger(ResetPage.class);

    private WebDriver driver;

    public ResetPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.reset-password"));

        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(id="passWord1")
    private WebElement passWord1;

    @FindBy(id="passWord2")
    private WebElement passWord2;

    @FindBy(id="reset")
    private WebElement reset;

    public void setPassword(String value) {
        this.passWord1.sendKeys(value);
        this.passWord2.sendKeys(value);
    }

    public SigninPage パスワードのリセット() throws Throwable {
        this.reset.click();

        NotifyDialogPage dialogPage = new NotifyDialogPage(this.driver);
        dialogPage.ok();

        return new SigninPage(this.driver);
    }
}
