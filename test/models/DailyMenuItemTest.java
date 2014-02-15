package models;

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
         start(fakeApplication());
     }

     @After
     public void tearDown() {
     }

     @Test
     public void dailyMenuは親となるDailyMenuオブジェクトを返すこと() {
         Ebean.save((List) Yaml.load("test-data.yml"));



    }
}
