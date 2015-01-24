package features.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
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

    public void サインイン(String email, String password) throws Throwable {
        userEmail.sendKeys(email);
        userPassword.sendKeys(password);
        signin.click();

        // サインインが終了し、次の画面に遷移する(=入力欄が消える)まで待つ
        Wait<WebDriver> wait = new WebDriverWait(driver, DRIVER_WAIT);
        wait.until(invisibilityOfElementLocated(By.id("user.email")));
    }

}
