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

        SeleniumUtils.waitForVisible(driver, By.cssSelector("nav.admin-header"));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.cssSelector("nav.admin-header"))), this);
        this.driver = driver;
    }

    @FindBy(how=How.LINK_TEXT, using="ダッシュボード")
    private WebElement showAdminIndex;

    @FindBy(how=How.LINK_TEXT, using="商品データ一覧")
    private WebElement showMenuItemsIndex;

    @FindBy(how=How.LINK_TEXT, using="メニュー作成")
    private WebElement showOrderMenuNew;

    @FindBy(how=How.LINK_TEXT, using="ユーザ一覧")
    private WebElement showUsersIndex;

    @FindBy(how=How.LINK_TEXT, using="設定")
    private WebElement showSettings;

    public features.pages.admin.IndexPage showAdminIndex() throws Throwable {
        this.showAdminIndex.click();
        return new features.pages.admin.IndexPage(this.driver);
    }

    public features.pages.admin.menuItem.IndexPage showMenuItemsIndex() throws Throwable {
        this.showMenuItemsIndex.click();
        return new features.pages.admin.menuItem.IndexPage(this.driver);
    }

    public features.pages.admin.dailyMenu.NewPage showOrderMenuNew() throws Throwable {
        this.showOrderMenuNew.click();
        return new features.pages.admin.dailyMenu.NewPage(this.driver);
    }

    public features.pages.admin.user.IndexPage showUsersIndex() throws Throwable {
        this.showUsersIndex.click();
        return new features.pages.admin.user.IndexPage(this.driver);
    }

    public features.pages.admin.SettingsPage showSettings() throws Throwable {
        this.showSettings.click();
        return new features.pages.admin.SettingsPage(this.driver);
    }
}
