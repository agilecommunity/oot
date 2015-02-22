package features.pages.admin;

import features.support.SeleniumUtils;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

public class PurchaseOrderConfirmationPage {

    private static Logger logger = LoggerFactory.getLogger(PurchaseOrderConfirmationPage.class);

    private WebDriver driver;

    public PurchaseOrderConfirmationPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-purchase-order-confirmation"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how= How.CSS, using="div.content.admin-purchase-order-confirmation")
    private WebElement base;

    public List<Map<String, String>> getList(DateTime orderDate) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        try {
            By noDataLocator = By.xpath(String.format("//div[@id='purchase-order-confirmation-%s']/div/p[contains(@class, 'no-data')]", orderDate.toString("yyyyMMdd")));
            this.base.findElement(noDataLocator);
            return list;
        } catch (NoSuchElementException ex) {
            // 次の処理にいくので、無視
        }

        By statusRowsLocator = By.xpath(String.format("//table[@id='purchase-order-confirmation-details-%s']/tbody/tr", orderDate.toString("yyyyMMdd")));
        List<WebElement> statusRows = this.base.findElements(statusRowsLocator);

        for (WebElement statusRow : statusRows) {
            Map<String, String> checkRow = new HashMap<String, String>();

            checkRow.put("レジ", statusRow.findElement(By.xpath("td[1]")).getText());
            checkRow.put("ショップ名", statusRow.findElement(By.xpath("td[2]")).getText());
            checkRow.put("No", statusRow.findElement(By.xpath("td[3]")).getText());
            checkRow.put("品名", statusRow.findElement(By.xpath("td[4]")).getText());
            checkRow.put("税抜", statusRow.findElement(By.xpath("td[5]")).getText());
            checkRow.put("税込", statusRow.findElement(By.xpath("td[6]")).getText());

            list.add(checkRow);
        }

        return list;
    }
}
