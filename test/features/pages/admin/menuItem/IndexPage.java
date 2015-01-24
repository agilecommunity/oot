package features.pages.admin.menuItem;

import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class IndexPage {

    private static Logger logger = LoggerFactory.getLogger(IndexPage.class);

    private WebDriver driver;

    public IndexPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.xpath("//button[text()='商品を追加する']"));

        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(id="addItem")
    private WebElement addItem;

    public EditPage addProduct(Map<String, String> params) throws Throwable {
        this.addItem.click();
        EditPage editPage = new EditPage(this.driver);

        return editPage;
    }
}
