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
import utils.controller.parameters.ParameterConverter;
import utils.snakeyaml.YamlUtil;

public class DailyMenuItemTest extends WithApplication {

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
    public void daily_menuは親となるDailyMenuオブジェクトを返すこと() {
        Ebean.save((List) YamlUtil.load("fixtures/test/menu_item.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_menu.yml"));
        Ebean.save((List) YamlUtil.load("fixtures/test/daily_menu_item.yml"));

        assertThat(DailyMenuItem.find.byId(1L).dailyMenu).isNotNull();
        assertThat(DailyMenuItem.find.byId(1L).dailyMenu.menuDate.toString()).isEqualTo(ParameterConverter.convertTimestampFrom("2014-02-10T00:00:00.000+0900").toString());
    }
}
