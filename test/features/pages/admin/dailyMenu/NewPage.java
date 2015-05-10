package features.pages.admin.dailyMenu;

import features.pages.admin.dailyOrder.EditPage;
import features.support.SeleniumUtils;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
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
import java.util.Locale;

public class NewPage {

    public class DatePickerModule {

        private Logger logger = LoggerFactory.getLogger(DatePickerModule.class);

        private WebDriver driver;

        public DatePickerModule(WebDriver driver) throws Throwable {
            SeleniumUtils.waitForVisible(driver, By.cssSelector("ul.dropdown-menu-datepicker"));
            PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.cssSelector("ul.dropdown-menu-datepicker"))), this);
            this.driver = driver;
        }

        @FindBy(how=How.CSS, using="table.tbl-datepicker-days")
        private WebElement daysTable;

        @FindBy(how=How.CSS, using="button.btn-datepicker-prev-month")
        private WebElement prevMonth;

        @FindBy(how=How.CSS, using="button.btn-datepicker-next-month")
        private WebElement nextMonth;

        @FindBy(how=How.CSS, using="button.btn-datepicker-toggle")
        private WebElement currentMonth;

        @FindBy(how=How.CSS, using="button.btn-datepicker-toggle")
        private WebElement switchScale;

        public void pickDate(DateTime value) throws Throwable {

            logger.debug("#pickDate targetDate: {} getDayOfMonth: {}", value, value.getDayOfMonth());
            logger.debug("#pickDate currentMonth: {}", currentMonth.getText());

            DateTime currentMonthDt = DateTimeFormat.forPattern("MMMM yyyy").withLocale(Locale.JAPAN).parseDateTime(currentMonth.getText());

            logger.debug("#pickDate currentMonth Year:{} Month:{}", currentMonthDt.getYear(), currentMonthDt.getMonthOfYear());

            int months = Months.monthsBetween(value.withDayOfMonth(1) , currentMonthDt.withDayOfMonth(1)).getMonths();

            logger.debug("#pickDate difference betweeen target:{} current:{} months:{}", value.withDayOfMonth(1), currentMonthDt.withDayOfMonth(1), months);

            if (months != 0) {
                WebElement targetOperator = null;
                if (months > 0) {
                    targetOperator = this.prevMonth;
                }
                if (months < 0) {
                    targetOperator = this.nextMonth;
                }

                for (int index = 0; index < Math.abs(months); index++) {
                    targetOperator.click();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        logger.warn("#pickDate", ex);
                    }
                }

                if (!currentMonth.getText().equals(value.toString("MMMM yyyy", Locale.JAPAN))) {
                    throw new Exception(String.format("目的の月に変更できませんでした current:[%s]", currentMonth.getText()));
                }
            }

            By dayLocator = By.xpath("tbody/tr/td/button[span[not(contains(@class, 'text-muted')) and text()='" + String.format("%02d", value.getDayOfMonth()) + "']]");
            WebElement targetDay = this.daysTable.findElement(dayLocator);

            targetDay.click();
        }
    }

    private static Logger logger = LoggerFactory.getLogger(NewPage.class);

    private WebDriver driver;

    public NewPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.category-row"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how=How.CSS, using="button.btn-show-calendar")
    private WebElement showCalendar;

    @FindBys(@FindBy(how=How.CSS, using="div.menu-statuses > button.btn"))
    private List<WebElement> menuStatues;

    @FindBy(how=How.CSS, using="button.btn-edit-order")
    private WebElement editOrder;

    public NewPage setDateByCalendar(DateTime value) throws Throwable {
        showCalendar.click();

        DatePickerModule pickerModule = new DatePickerModule(this.driver);
        pickerModule.pickDate(value);

        this.waitForSyncServer();

        By dayLocator = By.xpath(String.format("//ul[contains(@class,'day-tabs')]/li[@id='day-%s' and contains(@class, 'active')]/a", value.toString("yyyyMMdd")));
        SeleniumUtils.waitForVisible(this.driver, dayLocator);

        // 日付が切り替わると画面の再構築が行われるため、新しいページを返す
        return new NewPage(this.driver);
    }

    public NewPage setDate(DateTime value) throws Throwable {
        By dayLocator = By.xpath(String.format("//ul[contains(@class,'day-tabs')]/li[@id='day-%s']/a", value.toString("yyyyMMdd")));
        SeleniumUtils.waitForVisible(this.driver, dayLocator);
        SeleniumUtils.clickUsingJavaScript(this.driver, dayLocator); // 普通に clickUsingJavaScript() 呼んでも押せないのでJavaScriptを起動する

        this.waitForSyncServer();

        // 日付が切り替わると画面の再構築が行われるため、新しいページを返す
        return new NewPage(this.driver);
    }

    public void setStatus(String value) throws Throwable {

        if (value == null || value.isEmpty()) {
            return;
        }

        for (WebElement item : this.menuStatues) {
            if (item.getText().equals(value)) {
                item.click();

                this.waitForSyncServer();

                return;
            }
        }

        logger.warn("#setStatus can't find status: {}", value);
    }

    /**
     * 商品を追加する
     * @param shopName
     * @param itemName
     * @param reducedOnOrder  価格 単位を含めること (ex. "100円")
     * @throws Throwable
     */
    public void addItem(String category, String shopName, String itemName, String reducedOnOrder) throws Throwable {

        By emptyTileLocator = By.xpath(String.format("//div[contains(@class, 'category-row row') and div//div[text()='%s']]//div[contains(@class, 'menu-item-sm empty')][1]", category));
        SeleniumUtils.waitAndClick(this.driver, emptyTileLocator);

        SelectItemPage selectItemPage = new SelectItemPage(this.driver);
        SelectShopPage selectShopPage = selectItemPage.selectShop();
        selectShopPage.select(shopName);
        selectItemPage.select(itemName, reducedOnOrder);

        By itemTileLocator = By.xpath(String.format("//div[@class='menu-item-sm selected']/div[@class='tile']/div[@class='caption' and div[contains(@class, 'shop-name') and text()='%s'] and div[contains(@class, 'food-name') and text()='%s ' and span/span[text()='%s']]]", shopName, itemName, reducedOnOrder));
        SeleniumUtils.waitForVisible(this.driver, itemTileLocator);

        return;
    }

    public EditPage showEditOrder() throws Throwable {
        this.editOrder.click();

        return new features.pages.admin.dailyOrder.EditPage(this.driver);
    }

    private void waitForSyncServer() throws Throwable {
        // 処理中はスピナーが表示されるので、それが消えるまで待つ
        By spinnerLocator = By.cssSelector("span.us-spinner");
        SeleniumUtils.waitForInvisible(this.driver, spinnerLocator);
    }

}
