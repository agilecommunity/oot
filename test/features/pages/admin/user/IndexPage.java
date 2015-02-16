package features.pages.admin.user;

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

public class IndexPage {

    private static Logger logger = LoggerFactory.getLogger(IndexPage.class);

    private WebDriver driver;

    public IndexPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("table.table.users"));

        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how= How.CSS, using="div.content.admin-users-index")
    private WebElement base;

    public List<Map<String, String>> getList() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        By userRowsLocator = By.xpath("//table[contains(@class, 'table-striped users')]/tbody/tr");
        List<WebElement> userRows = this.base.findElements(userRowsLocator);

        for (WebElement userRow : userRows) {
            Map<String, String> row = new HashMap<String, String>();

            row.put("メールアドレス", userRow.findElement(By.xpath("td[1]")).getText());
            row.put("姓", userRow.findElement(By.xpath("td[2]")).getText());
            row.put("名", userRow.findElement(By.xpath("td[3]")).getText());
            row.put("管理者", userRow.findElement(By.xpath("td[4]")).getText());

            list.add(row);
        }

        return list;
    }

}
