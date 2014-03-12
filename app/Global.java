
import com.avaje.ebean.Ebean;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;

import models.DailyMenu;
import models.DailyMenuItem;
import models.MenuItem;
import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;
import play.Play;

public class Global extends GlobalSettings  {

    @Override
    public void onStart(Application app) {

        if(Play.isTest()) { // テストモードの場合はfixtureを読むようにしているので何もしない
            return;
        }

        if(Play.isDev()) {
            if(MenuItem.find.findRowCount() == 0) {
                Ebean.save((List) Yaml.load("fixtures/dev/menu_item.yml"));
                Ebean.save((List) Yaml.load("fixtures/dev/daily_menu.yml"));
                Ebean.save((List) Yaml.load("fixtures/dev/daily_menu_item.yml"));
            }
        }

    }

}
