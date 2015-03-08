package features.pages.dialog;

import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorDialogPage {
    private static Logger logger = LoggerFactory.getLogger(ErrorDialogPage.class);

    private static final String BASE_XPATH = "//div[@class='modal-dialog modal-lg' and div/div[contains(@class, 'modal-header dialog-header-error')]]";

    private WebDriver driver;

    public ErrorDialogPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.xpath(BASE_XPATH));

        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(xpath=BASE_XPATH)
    WebElement base;

    public void close() throws Throwable {
        this.base.findElement(By.xpath("div[@class='modal-content']//button[text()='Close']")).click();

        try {
            if (this.driver.findElement(By.xpath(BASE_XPATH)) != null) {
                SeleniumUtils.waitForInvisible(this.driver, By.xpath(BASE_XPATH));
            }
        } catch (NoSuchElementException ex) {
            // なくなっている場合は何もしなくてよい
        }
    }
}
