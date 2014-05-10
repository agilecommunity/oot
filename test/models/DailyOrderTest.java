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
public class DailyOrderTest extends WithApplication {

     @Before
     public void setUp() {
         start(fakeApplication(inMemoryDatabase()));
     }

     @After
     public void tearDown() {
     }

     @Test
     public void local_userはLocalUserオブジェクトを返すこと() {
         Ebean.save((List) Yaml.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/local_user.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order.yml"));
         Ebean.save((List) Yaml.load("fixtures/test/daily_order_item.yml"));


         assertThat(DailyOrder.find.byId(1L).local_user).isNotNull();
         assertThat(DailyOrder.find.byId(1L).local_user.id).isEqualTo("steve@foo.baa");
         assertThat(DailyOrder.find.byId(1L).local_user.first_name).isEqualTo("スティーブ");
    }
}
