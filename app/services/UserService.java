package services;

import java.util.List;

import models.LocalToken;
import models.LocalUser;

import org.joda.time.DateTime;

import play.Application;
import play.Logger;
import scala.Option;
import scala.Some;
import securesocial.core.AuthenticationMethod;
import securesocial.core.Identity;
import securesocial.core.IdentityId;
import securesocial.core.PasswordInfo;
import securesocial.core.SocialUser;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;

public class UserService extends BaseUserService {

    Logger.ALogger logger = play.Logger.of("application.services.UserService");

    public UserService(Application application) {
        super(application);
    }

    @Override
    public Identity doSave(Identity user) {
        logger.debug(String.format("#doSave userId:%s", user.identityId().userId()));

        LocalUser localUser = null;
        localUser = LocalUser.find.byId(user.identityId().userId());

        if (localUser == null) {
            logger.debug("#doSave LocalUser not found create new");
            localUser = new LocalUser();
            localUser.isAdmin = false;
            copyIdentityToLocalUser(user, localUser);
            localUser.save();
        } else {
            logger.debug("#doSave LocalUser found update localUser:" + localUser.toString());
            copyIdentityToLocalUser(user, localUser);
            localUser.update();
        }

        return user;
    }

    @Override
    public void doSave(Token token) {
        logger.debug(String.format("doSaveToken uuid:%s email:%s", token.uuid, token.email));

        LocalToken localToken = new LocalToken();

        localToken.uuid = token.uuid;
        localToken.email = token.email;
        localToken.createdAt = token.creationTime.toDate();
        localToken.expireAt = token.expirationTime.toDate();
        localToken.isSignUp = token.isSignUp;

        localToken.save();

        logger.debug(String.format("doSaveToken uuid:%s", localToken.uuid));
    }

    @Override
    public Identity doFind(IdentityId identityId) {
        logger.debug(String.format("doFind userId:%s", identityId.userId()));

        LocalUser localUser = LocalUser.find.byId(identityId.userId());

        if (localUser == null) {
            logger.debug("doFind LocalUser not found");
            return null;
        }

        SocialUser social_user = createSocialUserFrom(localUser);

        return social_user;
    }

    @Override
    public Token doFindToken(String tokenId) {
        logger.debug(String.format("doFindToken tokenId:%s", tokenId));

        LocalToken localToken = LocalToken.find.byId(tokenId);

        if (localToken == null) {
            logger.debug("doFindToken LocalToken not found");
            return null;
        }

        Token result = new Token();
        result.uuid = localToken.uuid;
        result.email = localToken.email;
        result.isSignUp = localToken.isSignUp;
        result.creationTime = new DateTime(localToken.createdAt);
        result.expirationTime = new DateTime(localToken.expireAt);

        return result;
    }

    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        logger.debug(String.format("doFindByEmailAndProvider email:%s providerId:%s", email, providerId));

        List<LocalUser> list = LocalUser.find.where().eq("email", email).eq("provider", providerId).findList();

        if (list.size() != 1) {
            logger.warn(String.format("doFindByEmailAndProvider LocalUser not found or too many email:%s providerId:%s list.size:%d",
                email, providerId, list.size()));
            return null;
        }

        SocialUser socialUser = createSocialUserFrom(list.get(0));

        return socialUser;
    }

    @Override
    public void doDeleteToken(String uuid) {
        logger.debug(String.format("doDeleteToken uuid:%s", uuid));

        LocalToken localToken = LocalToken.find.byId(uuid);

        if (localToken != null) {
            localToken.delete();
        }
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

    private void copyIdentityToLocalUser(Identity source, LocalUser dest) {
        dest.id = source.identityId().userId();
        dest.provider = source.identityId().providerId();
        dest.firstName = source.firstName();
        dest.lastName = source.lastName();
        dest.email = source.email().get();
        dest.password = source.passwordInfo().get().password();
    }

    private SocialUser createSocialUserFrom(LocalUser localUser) {
        SocialUser socialUser = new SocialUser(
                new IdentityId(localUser.id, localUser.provider)
              , localUser.firstName
              , localUser.lastName
              , String.format("%s %s", localUser.firstName, localUser.lastName)
              , Option.apply(localUser.email)
              , null
              , new AuthenticationMethod("userPassword")
              , null
              , null
              , Some.apply(new PasswordInfo("bcrypt", localUser.password, null))
          );

        return socialUser;
    }
}
