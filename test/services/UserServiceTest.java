package services;

import models.LocalToken;
import org.joda.time.DateTime;
import org.junit.Test;
import play.Application;
import play.test.FakeApplication;
import utils.Utils;

import java.util.UUID;

import static play.test.Helpers.*;
import static play.test.Helpers.fakeApplication;
import static org.fest.assertions.api.Assertions.assertThat;

public class UserServiceTest {

    @Test
    public void doDeleteExpiredTokensは保存期間を超えたTokenのみ削除すること() throws Throwable {

        FakeApplication app = fakeApplication(utils.Utils.getAdditionalApplicationSettings());
        start(app);
        Utils.cleanUpDatabase();

        UserService service = new UserService(new Application(app.getWrappedApplication()));

        Thread.sleep(2000); // doDeleteExpiredTokensが削除されるまで待つ

        LocalToken expiredToken = new LocalToken();
        expiredToken.uuid = UUID.randomUUID().toString();
        expiredToken.email = "expired@local";
        expiredToken.createdAt = DateTime.now().toDate();
        expiredToken.expireAt = DateTime.now().minusMinutes(2).toDate();
        expiredToken.isSignUp = true;
        expiredToken.save();

        LocalToken livingToken = new LocalToken();
        livingToken.uuid = UUID.randomUUID().toString();
        livingToken.email = "living@local";
        livingToken.createdAt = DateTime.now().toDate();
        livingToken.expireAt = DateTime.now().plusMinutes(2).toDate();
        livingToken.isSignUp = true;
        livingToken.save();

        service.doDeleteExpiredTokens();

        LocalToken expiredTokenActual = LocalToken.find.byId(expiredToken.uuid);
        assertThat(expiredTokenActual).describedAs("expiredToken").isNull();

        LocalToken livingTokenActual = LocalToken.find.byId(livingToken.uuid);

        assertThat(livingTokenActual).describedAs("livingToken").isNotNull();
        assertThat(livingTokenActual.uuid).describedAs("livingToken.uuid").isEqualTo(livingToken.uuid);

    }
}
