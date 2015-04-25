package models;

import static org.fest.assertions.api.Assertions.*;
import static play.test.Helpers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import play.test.FakeApplication;
import play.test.WithApplication;

import com.avaje.ebean.Ebean;
import utils.Utils;
import utils.snakeyaml.YamlUtil;

public class DailyOrderTest extends WithApplication {

    @Override
    protected FakeApplication provideFakeApplication() {
        return fakeApplication(utils.Utils.getAdditionalApplicationSettings());
    }

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        Utils.cleanUpDatabase();
    }

    @After
    @Override
    public void stopPlay() {
        super.stopPlay();
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
