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

public class OrderAggregatesPage {

    private static Logger logger = LoggerFactory.getLogger(OrderAggregatesPage.class);

    private WebDriver driver;

    public OrderAggregatesPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-order-aggregates"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how= How.CSS, using="div.content.admin-order-aggregates")
    private WebElement base;

    public List<Map<String, String>> getList() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        By statusRowsLocator = By.xpath("//table[@id='order-aggregates']/tbody/tr");
        List<WebElement> statusRows = this.base.findElements(statusRowsLocator);

        for (WebElement statusRow : statusRows) {
            Map<String, String> checkRow = new HashMap<String, String>();

            checkRow.put("コード", statusRow.findElement(By.xpath("td[1]")).getText());
            checkRow.put("注文数", statusRow.findElement(By.xpath("td[2]")).getText());

            list.add(checkRow);
        }

        return list;
    }
}
