
import java.util.List;

import models.MenuItem;
import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

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
                Ebean.save((List) Yaml.load("fixtures/dev/local_user.yml"));
            }
        }

    }

}
