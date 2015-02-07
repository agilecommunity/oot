package models;

import static org.fest.assertions.api.Assertions.*;
import static play.test.Helpers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import play.test.WithApplication;

import com.avaje.ebean.Ebean;
import utils.Utils;
import utils.snakeyaml.YamlUtil;

@RunWith(JUnit4.class)
public class DailyOrderTest extends WithApplication {

     @Before
     public void setUp() {
         start(fakeApplication(utils.Utils.getAdditionalApplicationSettings()));
         Utils.cleanUpDatabase();
     }

     @After
     public void tearDown() {
     }

     @Test
     public void local_userはLocalUserオブジェクトを返すこと() {
         Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
         Ebean.save((List) YamlUtil.load("fixtures/test/local_user.yml"));
         Ebean.save((List) YamlUtil.load("fixtures/test/daily_order.yml"));
         Ebean.save((List) YamlUtil.load("fixtures/test/daily_order_item.yml"));


         assertThat(DailyOrder.find.byId(1L).localUser).isNotNull();
         assertThat(DailyOrder.find.byId(1L).localUser.id).isEqualTo("steve@foo.bar");
         assertThat(DailyOrder.find.byId(1L).localUser.firstName).isEqualTo("スティーブ");
    }
}
