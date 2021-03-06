package features.pages.admin.checklist;

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

public class DailyPage {

    private static Logger logger = LoggerFactory.getLogger(DailyPage.class);

    private WebDriver driver;

    public DailyPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-checklist-daily"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how=How.CSS, using="div.content.admin-checklist-daily")
    private WebElement base;

    public String getTotalPriceOnOrder() throws Throwable {
        By locator = By.cssSelector("span.total-price-on-order.ng-binding");
        SeleniumUtils.waitForVisible(this.driver, locator);

        WebElement totalPriceOnOrder = base.findElement(locator);
        return totalPriceOnOrder.getText();
    }

    public List<Map<String, String>> getList() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        By itemNameCellsLocator = By.xpath("//table[@id='checklist']/thead/tr/th[contains(@class, 'food-name')]");
        By statusRowsLocator = By.xpath("//table[@id='checklist']/tbody/tr");

        List<WebElement> itemNameCells = this.base.findElements(itemNameCellsLocator);
        List<WebElement> statusRows = this.base.findElements(statusRowsLocator);

        for (WebElement statusRow : statusRows) {
            Map<String, String> checkRow = new HashMap<String, String>();

            checkRow.put("番号", statusRow.findElement(By.xpath("td[1]")).getText());
            checkRow.put("チェック", statusRow.findElement(By.xpath("td[2]")).getText());
            checkRow.put("氏名", statusRow.findElement(By.xpath("td[3]")).getText());

            for (int index=0; index<itemNameCells.size(); index++) {
                String shopName = itemNameCells.get(index).findElement(By.cssSelector("span.shop-name")).getText();
                String itemName = itemNameCells.get(index).findElement(By.cssSelector("span.food-name-with-reduced-price")).getText();

                checkRow.put(String.format("%s　%s", shopName, itemName), statusRow.findElement(By.xpath(String.format("td[%d]", index + 4))).getText());
            }

            list.add(checkRow);
        }

        return list;
    }

}
