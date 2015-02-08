package features.pages.admin;

import features.support.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChecklistPage {

    private static Logger logger = LoggerFactory.getLogger(ChecklistPage.class);

    private WebDriver driver;

    public ChecklistPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-checklist"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how=How.CSS, using="div.content.admin-checklist")
    private WebElement base;

    @FindBy(how=How.CLASS_NAME, using="total-price-on-order")
    private WebElement totalPriceOnOrder;

    public String getTotalPriceOnOrder() {
        return totalPriceOnOrder.getText();
    }

    public List<Map<String, String>> getList() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        By shopNameCellsLocator = By.xpath("//table[@id='checklist']/thead/tr/th[contains(@class, 'shop-name')]");
        By itemNameCellsLocator = By.xpath("//table[@id='checklist']/thead/tr/th[contains(@class, 'food-name')]");
        By statusRowsLocator = By.xpath("//table[@id='checklist']/tbody/tr");

        List<WebElement> shopNameCells = this.base.findElements(shopNameCellsLocator);
        List<WebElement> itemNameCells = this.base.findElements(itemNameCellsLocator);
        List<WebElement> statusRows = this.base.findElements(statusRowsLocator);

        for (WebElement statusRow : statusRows) {
            Map<String, String> checkRow = new HashMap<String, String>();

            checkRow.put("チェック", statusRow.findElement(By.xpath("td[1]")).getText());
            checkRow.put("氏名", statusRow.findElement(By.xpath("td[2]")).getText());

            for (int index=0; index<shopNameCells.size(); index++) {
                String shopName = shopNameCells.get(index).getText();
                String itemName = itemNameCells.get(index).getText();

                checkRow.put(String.format("%s　%s", shopName, itemName), statusRow.findElement(By.xpath(String.format("td[%d]", index + 3))).getText());
            }

            list.add(checkRow);
        }

        return list;
    }

}
