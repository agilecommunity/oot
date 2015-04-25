package securesocial.custom.services;

import java.util.List;

import models.LocalToken;
import models.LocalUser;

import org.joda.time.DateTime;

import play.Logger;
import play.Play;
import play.libs.F;
import scala.Option;
import scala.Some;
import securesocial.core.AuthenticationMethod;
import securesocial.core.BasicProfile;
import securesocial.core.PasswordInfo;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;
import securesocial.core.providers.UsernamePasswordProvider$;
import securesocial.core.services.SaveMode;
import utils.securesocial.BasicProfileBasedOperations;

public class MyUserService extends BaseUserService<LocalUser> {

    Logger.ALogger logger = play.Logger.of("application.MyUserService");

    @Override
    public F.Promise<LocalUser> doSave(BasicProfile user, SaveMode mode) {
        logger.debug("#doSave userId:{} mode:{}", user.userId(), mode.name());

        LocalUser localUser = LocalUser.find.byId(user.userId());

        if (mode == SaveMode.SignUp()) {
            if (localUser != null) {
                throw new RuntimeException(String.format("user is already exists. userId:{}", user.userId()));
            }

            logger.debug("#doSave SignUp create new");
            localUser = new LocalUser();
            localUser.isAdmin = false;
            copyBasicProfileToLocalUser(user, localUser);
            localUser.save();
        } else if (mode == SaveMode.LoggedIn()) {
            // TODO: 実装方法が分からない
        } else if (mode == SaveMode.PasswordChange()) {
            if (localUser == null) {
                throw new RuntimeException(String.format("user isnot found. userId:{}", user.userId()));
            }

            if (!Play.isProd()) {
                logger.debug("#doSave old password: {}", localUser.password);
                logger.debug("#doSave new password: {}", user.passwordInfo().get().password());
            }

            localUser.password = user.passwordInfo().get().password();
            localUser.update();

        } else {
            throw new RuntimeException("Unknown mode");
        }

        return F.Promise.pure(localUser);
    }

    @Override
    public F.Promise<BasicProfile> doFind(String providerId, String userId) {
        logger.debug("#doFind providerId:{} userId:{}", providerId, userId);

        LocalUser localUser = LocalUser.findbyProviderIdAndUserId(providerId, userId);

        if (localUser == null) {
            logger.debug("#doFind LocalUser not found");
            return F.Promise.pure(null);
        }

        if (!Play.isProd()) {
            logger.debug("#doFind password: {}", localUser.password);
        }

        BasicProfile basicProfile = BasicProfileBasedOperations.createProfile(localUser, AuthenticationMethod.UserPassword());

        return F.Promise.pure(basicProfile);
    }

    @Override
    public F.Promise<Token> doFindToken(String tokenId) {
        logger.debug("#doFindToken tokenId: {}", tokenId);

        LocalToken localToken = LocalToken.find.byId(tokenId);

        if (localToken == null) {
            logger.debug("#doFindToken LocalToken not found");
            return F.Promise.pure(null);
        }

        Token result = new Token();
        result.uuid = localToken.uuid;
        result.email = localToken.email;
        result.isSignUp = localToken.isSignUp;
        result.creationTime = new DateTime(localToken.createdAt);
        result.expirationTime = new DateTime(localToken.expireAt);

        return F.Promise.pure(result);
    }

    @Override
    public F.Promise<Token> doSaveToken(Token token) {
        logger.debug("#doSaveToken uuid: {} email: {}", token.uuid, token.email);

        LocalToken localToken = new LocalToken();

        localToken.uuid = token.uuid;
        localToken.email = token.email;
        localToken.createdAt = token.creationTime.toDate();
        localToken.expireAt = token.expirationTime.toDate();
        localToken.isSignUp = token.isSignUp;

        localToken.save();

        logger.debug("#doSaveToken uuid: {}", localToken.uuid);

        return F.Promise.pure(token);
    }

    @Override
    public F.Promise<LocalUser> doLink(LocalUser current, BasicProfile to) {
        // TODO: 現時点ではUserPass以外の認証をしないので、何もしないことにする
        return F.Promise.pure(current);
    }

    @Override
    public F.Promise<PasswordInfo> doPasswordInfoFor(LocalUser user) {
        logger.debug("#doPasswordInfoFor user.id: {}", user.id);
        throw new RuntimeException("doPasswordInfoFor is not implemented yet");
    }

    @Override
    public F.Promise<BasicProfile> doUpdatePasswordInfo(LocalUser user, PasswordInfo info) {
        logger.debug("#doUpdatePasswordInfo user.id: {}", user.id);
        throw new RuntimeException("doUpdatePasswordInfo is not implemented yet");
    }

    @Override
    public F.Promise<BasicProfile> doFindByEmailAndProvider(String email, String providerId) {
        logger.debug("#doFindByEmailAndProvider email: {} providerId: {}", email, providerId);

        LocalUser localUser = LocalUser.findbyProviderIdAndUserId(providerId, email);

        BasicProfile basicProfile = null;
        if (localUser != null) {
            basicProfile = BasicProfileBasedOperations.createProfile(localUser, AuthenticationMethod.UserPassword());
        }

        logger.debug("#doFindByEmailAndProvider basicProfile exists: {}", basicProfile != null);

        return F.Promise.pure(basicProfile);
    }

    @Override
    public F.Promise<Token> doDeleteToken(String uuid) {
        logger.debug("#doDeleteToken uuid: {}", uuid);

        LocalToken localToken = LocalToken.find.byId(uuid);
        Token token = new Token();

        token.uuid = localToken.uuid;
        token.email = localToken.email;
        token.creationTime = new DateTime(localToken.createdAt);
        token.expirationTime = new DateTime(localToken.expireAt);
        token.isSignUp = localToken.isSignUp;

        if (localToken != null) {
            localToken.delete();
        }

        return F.Promise.pure(token);
    }

    @Override
    public void doDeleteExpiredTokens() {
        logger.debug("#doDeleteExpiredTokens");

        List<LocalToken> list = LocalToken.find.where().lt("expireAt", new DateTime()).findList();

        logger.debug("#doDeleteExpiredTokens num of expire: {}", list.size());

        for(LocalToken localToken : list) {
            logger.debug("#doDeleteExpiredTokens expireAt: {}", localToken.expireAt);
            localToken.delete();
        }
    }

    private void copyBasicProfileToLocalUser(BasicProfile source, LocalUser dest) {
        dest.id = source.userId();
        dest.provider = source.providerId();
        dest.firstName = source.firstName().get();
        dest.lastName = source.lastName().get();
        dest.email = source.email().get();
        dest.password = source.passwordInfo().get().password();
    }
}
