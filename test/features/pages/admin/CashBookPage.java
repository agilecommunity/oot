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

public class CashBookPage {
    private static Logger logger = LoggerFactory.getLogger(CashBookPage.class);

    private WebDriver driver;

    public CashBookPage(WebDriver driver) throws Throwable {
        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.content.admin-cash-book"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how= How.CSS, using="div.content.admin-cash-book")
    private WebElement base;

    public List<Map<String, String>> getList(DateTime orderDate) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        try {
            By noDataLocator = By.xpath(String.format("//div[@id='cash-book-%s']/div/p[contains(@class, 'no-data')]", orderDate.toString("yyyyMMdd")));
            this.base.findElement(noDataLocator);
            return list;
        } catch (NoSuchElementException ex) {
            // 次の処理にいくので、無視
        }

        By statusRowsLocator = By.xpath(String.format("//table[@id='cash-book-details-%s']/tbody/tr", orderDate.toString("yyyyMMdd")));
        List<WebElement> statusRows = this.base.findElements(statusRowsLocator);

        for (WebElement statusRow : statusRows) {
            Map<String, String> checkRow = new HashMap<String, String>();

            checkRow.put("コード", statusRow.findElement(By.xpath("td[1]")).getText());
            checkRow.put("数量", statusRow.findElement(By.xpath("td[2]")).getText());

            list.add(checkRow);
        }

        return list;
    }
}
