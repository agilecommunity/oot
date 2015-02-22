package features.pages.admin.menuItem;

import features.support.SeleniumUtils;
import org.openqa.selenium.By;
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
    private WebElement shopName;

    @FindBy(id="menuItem.register_number")
    private WebElement registerNumber;

    @FindBys(@FindBy(how=How.XPATH, using="//button[contains(@class, 'category')]"))
    private List<WebElement> categories;

    @FindBy(id="menuItem.item_number")
    private WebElement itemNumber;

    @FindBy(id="menuItem.name")
    private WebElement name;

    @FindBy(id="menuItem.fixed_on_order")
    private WebElement fixedOnOrder;

    @FindBy(id="menuItem.discount_on_order")
    private WebElement discountOnOrder;

    @FindBy(id="menuItem.fixed_on_purchase_exc_tax")
    private WebElement fixedOnPurchaseExcTax;

    @FindBy(id="menuItem.fixed_on_purchase_inc_tax")
    private WebElement fixedOnPurchaseIncTax;

    @FindBy(id="menuItem.code")
    private WebElement code;

    @FindBys(@FindBy(how=How.XPATH, using="//button[contains(@class, 'status')]"))
    private List<WebElement> statuses;

    @FindBy(id="menuItem.ok")
    private WebElement ok;

    @FindBy(id="menuItem.cancel")
    private WebElement cancel;

    public void setShopName(String value) {
        this.shopName.sendKeys(value);
    }

    public void setRegisterNumber(String value) {
        this.registerNumber.sendKeys(value);
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

    public void setItemNumber(String value) {
        this.itemNumber.sendKeys(value);
    }

    public void setName(String value) {
        this.name.sendKeys(value);
    }

    public void setFixedOnOrder(String value) {
        this.fixedOnOrder.sendKeys(value);
    }

    public void setDiscountOnOrder(String value) {
        this.discountOnOrder.sendKeys(value);
    }

    public void setFixedOnPurchaseExcTax(String value) {
        this.fixedOnPurchaseExcTax.sendKeys(value);
    }

    public void setFixedOnPurchaseIncTax(String value) {
        this.fixedOnPurchaseIncTax.sendKeys(value);
    }

    public void setCode(String value) {
        this.code.sendKeys(value);
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
