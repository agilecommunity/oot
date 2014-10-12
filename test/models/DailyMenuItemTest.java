package models;

import static org.fest.assertions.api.Assertions.*;
import static play.test.Helpers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import play.libs.Yaml;
import play.test.WithApplication;

import com.avaje.ebean.Ebean;

@RunWith(JUnit4.class)
public class DailyMenuItemTest extends WithApplication {

     @Before
     public void setUp() {
         start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
     }

     @After
     public void tearDown() {
     }

     @Test
     public void daily_menuは親となるDailyMenuオブジェクトを返すこと() {
         Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_menu.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_menu_item.yml"));

         assertThat(DailyMenuItem.find.byId(1L).daily_menu).isNotNull();
         assertThat(DailyMenuItem.find.byId(1L).daily_menu.menu_date).isEqualTo("2014-02-10");
    }
}
