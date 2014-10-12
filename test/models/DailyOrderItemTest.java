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
public class DailyOrderItemTest extends WithApplication {

     @Before
     public void setUp() {
         start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
     }

     @After
     public void tearDown() {
     }

     @Test
     public void daily_orderは親となるDailyOrderオブジェクトを返すこと() {
         Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));


         assertThat(DailyOrderItem.find.byId(1L).daily_order).isNotNull();
         assertThat(DailyOrderItem.find.byId(1L).daily_order.order_date).isEqualTo("2014-02-10");
    }
}
