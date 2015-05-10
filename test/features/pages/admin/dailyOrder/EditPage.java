package features.pages.admin.dailyOrder;

import features.pages.SelectNumOrdersPage;
import features.pages.admin.SelectUserPage;
import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EditPage {

    private static Logger logger = LoggerFactory.getLogger(EditPage.class);

    private static final String BASE_XPATH = "//div[contains(@class,'modal-dialog') and div[@class='modal-content']/div[contains(@class, 'modal-header')]/h4[contains(text(), '注文修正')]]";

    private WebDriver driver;

    public EditPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.xpath(BASE_XPATH));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.xpath(BASE_XPATH))), this);
        this.driver = driver;
    }

    @FindBy(how= How.ID, using="table_edit_order")
    private WebElement table;

    @FindBy(how= How.CSS, using="button.btn-add-user")
    private WebElement addUser;

    @FindBy(how= How.CSS, using="button.close")
    WebElement close;

    /**
     * 注文を追加する
     * @param userName ユーザ名 ("{姓} {名}"のフォーマットで指定)
     * @param itemName 商品名 ("{店名} {商品番号} {商品名} {値引きを含めた値段}"のフォーマットで指定)
     * @param numOrders 注文数
     * @throws Throwable
     */
    public void addOrder(String userName, String itemName, Integer numOrders) throws Throwable {

        try {
            By nameLocator = By.xpath(String.format("tbody/tr[td[contains(@class, 'user-name') and text()='%s']]", userName));
            table.findElement(nameLocator);
        } catch (NoSuchElementException ex) {
            this.addUser.click();
            SelectUserPage selectUserPage = new SelectUserPage(this.driver);
            selectUserPage.select(userName);
        }

        By itemNameCellsLocator = By.xpath("thead/tr/th[contains(@class, 'food-name')]");
        List<WebElement> itemNameCells = table.findElements(itemNameCellsLocator);

        Integer itemPosition = -1;
        for (int index=0; index<itemNameCells.size(); index++) {
            if (itemNameCells.get(index).getText().equals(itemName)) {
                itemPosition = index;
                break;
            }
        }

        By cellLocator = By.xpath(String.format("tbody/tr[td[contains(@class, 'user-name') and text()='%s']]/td[contains(@class, 'order-status')][%d]", userName, itemPosition + 1));
        WebElement cell = table.findElement(cellLocator);
        cell.click();

        SelectNumOrdersPage selectNumOrdersPage = new SelectNumOrdersPage(this.driver);
        selectNumOrdersPage.select(numOrders);

        this.waitForSyncServer();
    }

    public void close() throws Throwable {
        this.close.click();

        SeleniumUtils.waitForInvisible(this.driver, By.xpath(BASE_XPATH));
    }

    private void waitForSyncServer() throws Throwable {
        // 処理中はスピナーが表示されるので、それが消えるまで待つ
        By spinnerLocator = By.cssSelector("span.us-spinner");
        SeleniumUtils.waitForInvisible(this.driver, spinnerLocator);
    }
}
