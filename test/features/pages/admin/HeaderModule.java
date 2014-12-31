package features.pages.admin;

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

public class HeaderModule {

    private static Logger logger = LoggerFactory.getLogger(HeaderModule.class);

    private WebDriver driver;

    public HeaderModule(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.order-management"));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.cssSelector("nav.admin-header"))), this);
        this.driver = driver;
    }

    @FindBy(how=How.LINK_TEXT, using="商品データ一覧")
    private WebElement showMenuItemsIndex;

    public void 商品一覧画面表示() {
        this.showMenuItemsIndex.click();
    }

}
