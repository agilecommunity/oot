
import java.math.BigDecimal;
import java.util.GregorianCalendar;

import models.DailyMenu;
import models.DailyMenuItem;
import models.MenuItem;
import play.Application;
import play.GlobalSettings;
import play.Play;

public class Global extends GlobalSettings  {

    @Override
    public void onStart(Application app) {

        if(Play.isTest()) { // テストモードの場合はfixtureを読むようにしているので何もしない
            return;
        }

        if(MenuItem.find.findRowCount() == 0) {
            MenuItem item = new MenuItem();
            item.category = "bento";
            item.shop_name = "MooMoo Shop";
            item.name = "MooMoo Pizza";
            item.price_on_order = new BigDecimal("2130");
            item.item_image_path = "/data/lunch/images/1-1.png";
            item.save();

            DailyMenu menu = new DailyMenu();
            menu.menu_date = new GregorianCalendar(2014, 2 - 1, 1).getTime();
            menu.status = "open";

            DailyMenuItem dMenuItem = new DailyMenuItem();
            dMenuItem.menuItem = item;

            menu.detailItems.add(dMenuItem);

            menu.save();

        }
    }

}