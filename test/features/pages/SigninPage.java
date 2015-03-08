package features.pages;

import features.pages.dialog.ErrorDialogPage;
import features.pages.resetPassword.StartResetPage;
import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class SigninPage {

    private static Integer DRIVER_WAIT = 10; // タイムアウト

    private static Logger logger = LoggerFactory.getLogger(SigninPage.class);

    private WebDriver driver;

    public SigninPage(WebDriver driver) {

        Wait<WebDriver> wait = new WebDriverWait(driver, DRIVER_WAIT);
        wait.until(visibilityOfElementLocated(By.id("user.email")));

        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(id="user.email")
    private WebElement userEmail;

    @FindBy(id="user.password")
    private WebElement userPassword;

    @FindBy(id="signin")
    private WebElement signin;

    @FindBy(id="start_reset")
    private WebElement startReset;

    public void サインイン(String email, String password) throws Throwable {
        this.userEmail.sendKeys(email);
        this.userPassword.sendKeys(password);
        this.signin.click();

        // サインインが終了し、次の画面に遷移する(=入力欄が消える)まで待つ
        SeleniumUtils.waitForInvisible(this.driver, By.id("user.email"));
    }

    public ErrorDialogPage サインイン_ExpectingFailure(String email, String password) throws Throwable {
        this.userEmail.sendKeys(email);
        this.userPassword.sendKeys(password);
        this.signin.click();

        return new ErrorDialogPage(this.driver);
    }

    public StartResetPage パスワードのリセット() throws Throwable {
        this.startReset.click();

        SeleniumUtils.waitForInvisible(this.driver, By.id("user.email"));

        return new StartResetPage(this.driver);
    }

}
