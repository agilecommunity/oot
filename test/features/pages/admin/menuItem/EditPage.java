package features.pages.admin.menuItem;

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

    private WebDriver driver;

    public EditPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.xpath("//div[contains(@class, 'modal-header')]/h4[text()='商品編集']"));

        PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.cssSelector("div.modal-dialog"))), this);
        this.driver = driver;
    }

    @FindBy(id="menuItem.shop_name")
    private WebElement shop_name;

    @FindBys(@FindBy(how=How.XPATH, using="//button[contains(@class, 'category')]"))
    private List<WebElement> categories;

    @FindBy(id="menuItem.name")
    private WebElement name;

    @FindBy(id="menuItem.price_on_order")
    private WebElement priceOnOrder;

    @FindBy(id="menuItem.code")
    private WebElement code;

    @FindBys(@FindBy(how=How.XPATH, using="//button[contains(@class, 'status')]"))
    private List<WebElement> statuses;

    @FindBy(id="menuItem.ok")
    private WebElement ok;

    @FindBy(id="menuItem.cancel")
    private WebElement cancel;

    public void setShopName(String value) {
        this.shop_name.sendKeys(value);
    }

    public void setCategory(String value) {

        logger.debug("#setCategory value: '{}'", value);

        for (WebElement item : this.categories) {

            logger.debug("#setCategory category: '{}'", item.getText());

            if (value.equals(item.getText())) {
                item.click();
                return;
            }
        }

        throw new IllegalArgumentException("存在しないカテゴリです: " + value);
    }

    public void setPriceOnOrder(String value) {
        this.priceOnOrder.sendKeys(value);
    }

    public void setCode(String value) {
        this.code.sendKeys(value);
    }

    public void setName(String value) {
        this.name.sendKeys(value);
    }

    public void setStatus(String value) {

        for (WebElement item : this.statuses) {

            if (value.equals(item.getText())) {
                item.click();
                return;
            }
        }

    }

    public void save() {
        this.ok.click();
    }
}