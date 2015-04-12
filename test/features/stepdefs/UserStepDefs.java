package features.stepdefs;

import cucumber.api.DataTable;
import cucumber.api.java.ja.ならば;
import cucumber.api.java.ja.もし;
import features.pages.OrderPage;
import features.support.CucumberUtils;
import features.support.WebBrowser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public class UserStepDefs {

    @もし("^以下の内容で注文する:$")
    public void 以下の内容で注文する(Map<String, String> orderParams) throws Throwable {

        OrderPage orderPage = new OrderPage(WebBrowser.INSTANCE);

        DateTime orderDate = CucumberUtils.parseDate(orderParams.get("日付"));

        int itemIndex = 1;
        do {
            String keyName = String.format("商品-%d", itemIndex);

            if (!orderParams.containsKey(keyName)) {
                break;
            }

            String[] itemNameParams = orderParams.get(keyName).split("　", 3);
            orderPage.order(orderDate, itemNameParams[0], itemNameParams[1], itemNameParams[2]);

            itemIndex += 1;
        } while (true);

    }

    @ならば("^ギャザリングの状況が以下であること:$")
    public void ギャザリングの状況が以下であること(DataTable gatheringStatuses) throws Throwable {

        OrderPage orderPage = new OrderPage(WebBrowser.INSTANCE);

        List<Map<String, String>> gatheringStatusList = gatheringStatuses.asMaps(String.class, String.class);
        for (Map<String, String>gatheringStatus : gatheringStatusList) {
            DateTime orderDate = CucumberUtils.parseDate(gatheringStatus.get("日付"));

            String actualStatus = orderPage.getGatheringStatus(orderDate);

            assertThat(actualStatus).describedAs("表示内容").isEqualTo(gatheringStatus.get("表示内容"));
        }


    }
}
