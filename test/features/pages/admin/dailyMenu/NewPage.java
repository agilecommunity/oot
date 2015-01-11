package features.pages.admin.dailyMenu;

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
            SeleniumUtils.waitForVisible(driver, By.cssSelector("div.datepicker"));
            PageFactory.initElements(new DefaultElementLocatorFactory(driver.findElement(By.cssSelector("div.datepicker"))), this);
            this.driver = driver;
        }

        @FindBy(how=How.CSS, using="div.datepicker-days > table")
        private WebElement daysTable;

        @FindBy(how=How.CSS, using="div.datepicker-days > table > thead > tr > th.prev")
        private WebElement prevMonth;

        @FindBy(how=How.CSS, using="div.datepicker-days > table > thead > tr > th.next")
        private WebElement nextMonth;

        @FindBy(how=How.CSS, using="div.datepicker-days > table > thead > tr > th.switch")
        private WebElement currentMonth;

        @FindBy(how=How.CSS, using="div.datepicker-days > table > thead > tr > th.switch")
        private WebElement switchScale;

        public void pickDate(DateTime value) throws Throwable {

            logger.debug("#pickDate currentMonth:" +  currentMonth.getText());

            DateTime currentMonthDt = DateTimeFormat.forPattern("MMMM yyyy").withLocale(Locale.US).parseDateTime(currentMonth.getText());

            logger.debug("#pickDate currentMonth Year:{} Month:{}", currentMonthDt.getYear(), currentMonthDt.getMonthOfYear());

            int months = Months.monthsBetween(value.withDayOfMonth(1) , currentMonthDt.withDayOfMonth(1)).getMonths();

            logger.debug("#pickDate difference betweeen target:{} current:{} months:{}", value.withDayOfMonth(1), currentMonthDt.withDayOfMonth(1), months);

            if (months == 0) {
                return;
            }

            WebElement targetOperator = null;
            if (months > 0) {
                targetOperator = this.prevMonth;
            }
            if (months < 0) {
                targetOperator = this.nextMonth;
            }

            for(int index=0; index<Math.abs(months); index++) {
                targetOperator.click();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    logger.warn("#pickDate", ex);
                }
            }

            if (!currentMonth.getText().equals(value.toString("MMMM yyyy", Locale.US))) {
                throw new Exception(String.format("目的の月に変更できませんでした current:[%s]", currentMonth.getText()));
            }

            By dayLocator = By.xpath("tbody/tr/td[@class='day' and text()='" + value.getDayOfMonth() + "']");
            WebElement targetDay = this.daysTable.findElement(dayLocator);

            targetDay.click();
        }
    }

    private static Logger logger = LoggerFactory.getLogger(NewPage.class);

    private WebDriver driver;

    public NewPage(WebDriver driver) throws Throwable {

        SeleniumUtils.waitForVisible(driver, By.cssSelector("div.selected-bento"));
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    @FindBy(how=How.CSS, using="button.show-calendar")
    private WebElement showCalendar;

    @FindBy(how=How.CSS, using="div#datetimepicker > input")
    private WebElement calendarDate;

    @FindBys(@FindBy(how=How.CSS, using="div.menu-statuses > button.btn"))
    private List<WebElement> menuStatues;

    public void setWeek(DateTime value) throws Throwable {
        showCalendar.click();

        DatePickerModule pickerModule = new DatePickerModule(this.driver);
        pickerModule.pickDate(value);
    }

    public void setDate(DateTime value) throws Throwable {
        By dayLocator = By.xpath(String.format("//ul[contains(@class,'day-tabs')]/li[@id='day-%s']/a", value.toString("yyyyMMdd")));
        SeleniumUtils.waitForVisible(this.driver, dayLocator);
        SeleniumUtils.click(this.driver, dayLocator); // 普通に click() 呼んでも押せないのでJavaScriptを起動する
    }

    public void setStatus(String value) throws Throwable {

        if (value == null || value.isEmpty()) {
            return;
        }

        for (WebElement item : this.menuStatues) {
            if (item.getText().equals(value)) {
                item.click();
                return;
            }
        }

        logger.warn("#setStatus can't find status: {}", value);
    }

    public void addItem(String shopName, String itemName) throws Throwable {

        By emptyTileLocator = By.cssSelector("div.tile.empty");

        WebElement emptyTile = this.driver.findElement(emptyTileLocator);
        emptyTile.click();

        SelectItemPage selectItemPage = new SelectItemPage(this.driver);
        SelectShopPage selectShopPage = selectItemPage.selectShop();
        selectShopPage.select(shopName);
        selectItemPage.select(itemName);

        By itemTileLocator = By.xpath(String.format("//div[@class='tile selected']/div[@class='menu-item']/div[@class='caption' and div[contains(@class, 'shop-name') and text()='【%s】'] and div[contains(@class, 'name')  and text()='%s']]", shopName, itemName));
        SeleniumUtils.waitForVisible(this.driver, itemTileLocator);

        return;
    }

}
