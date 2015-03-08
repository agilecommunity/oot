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

public class StartResetPage {

    private static Logger logger = LoggerFactory.getLogger(StartResetPage.class);

    private WebDriver driver;

    public StartResetPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.start-reset-password"));

        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(id="email")
    private WebElement email;

    @FindBy(id="startReset")
    private WebElement startReset;

    public void setEmail(String value) {
        this.email.sendKeys(value);
    }

    public SigninPage メールの送付() throws Throwable {
        this.startReset.click();

        NotifyDialogPage dialogPage = new NotifyDialogPage(this.driver);
        dialogPage.ok();

        return new SigninPage(this.driver);
    }
}


