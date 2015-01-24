package features.stepdefs;

import cucumber.api.java.ja.もし;
import features.pages.OrderPage;
import features.support.WebBrowser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Map;

public class UserStepDefs {

    @もし("^以下の内容で注文する:$")
    public void 以下の内容で注文する(Map<String, String> orderParams) throws Throwable {

        OrderPage orderPage = new OrderPage(WebBrowser.INSTANCE);

        DateTime orderDate = DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(orderParams.get("日付"));

        int itemIndex = 1;
        do {
            String keyName = String.format("商品-%d", itemIndex);

            if (!orderParams.containsKey(keyName)) {
                break;
            }

            String[] itemNameParams = orderParams.get(keyName).split("　", 2);
            orderPage.order(orderDate, itemNameParams[0], itemNameParams[1]);

            itemIndex += 1;
        } while (true);

    }

}
