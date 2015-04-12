package features.pages.admin;

import features.pages.dialog.NotifyDialogPage;
import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class SettingsPage {

    private static Logger logger = LoggerFactory.getLogger(SettingsPage.class);

    private WebDriver driver;

    public SettingsPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-settings"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how= How.CSS, using="div.content.admin-settings")
    public WebElement base;

    @FindBy(how=How.ID, using="gatheringSetting.enabled")
    public WebElement gatheringSettingEnabled;

    @FindBy(how=How.ID, using="gatheringSetting.minOrders")
    public WebElement gatheringSettingMinOrders;

    @FindBy(how=How.ID, using="gatheringSetting.discountPrice")
    public WebElement gatheringSettingDiscountPrice;

    @FindBy(how=How.ID, using="gatheringSetting.save")
    public WebElement gatheringSettingSave;

    public void setGatheringSettingEnabled(Boolean value) {
        logger.debug("#setGatheringSettingEnabled value:{} current{}", value, this.getGatheringSettingEnabled());

        if (this.getGatheringSettingEnabled() != value) {
            this.gatheringSettingEnabled.click();
        }
    }

    public void saveGatheringSetting() throws Throwable {
        this.gatheringSettingSave.click();

        NotifyDialogPage dialogPage = new NotifyDialogPage(this.driver);
        dialogPage.ok();
    }

    public void setGatheringSettingMinOrders(Integer value) {
        this.gatheringSettingMinOrders.clear();
        this.gatheringSettingMinOrders.sendKeys(value.toString());
    }

    public void setGatheringSettingDiscountPrice(BigDecimal value) {
        this.gatheringSettingDiscountPrice.clear();
        this.gatheringSettingDiscountPrice.sendKeys(value.toPlainString());
    }

    public Boolean getGatheringSettingEnabled() {
        return this.gatheringSettingEnabled.isSelected();
    }

    public Integer getGatheringSettingMinOrders() {
        String value = this.gatheringSettingMinOrders.getText();
        if (value == null || value.length() == 0) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public BigDecimal getGatheringSettingDiscountPrice() {
        String value = this.gatheringSettingDiscountPrice.getText();
        if (value == null || value.length() == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(this.gatheringSettingDiscountPrice.getText());
    }
}
