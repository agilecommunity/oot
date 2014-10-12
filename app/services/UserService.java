package services;

import java.sql.Timestamp;
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
        logger.debug(String.format("doSave userId:%s", user.identityId().userId()));

        LocalUser local_user = null;
        local_user = LocalUser.find.byId(user.identityId().userId());

        if (local_user == null) {
            logger.debug("doSave LocalUser not found create new");
            local_user = new LocalUser();
            copyIdentityToLocalUser(user, local_user);
            local_user.save();
        } else {
            logger.debug("doSave LocalUser found update");
            copyIdentityToLocalUser(user, local_user);
            local_user.update();
        }

        return user;
    }

    @Override
    public void doSave(Token token) {
        logger.debug(String.format("doSaveToken uuid:%s email:%s", token.uuid, token.email));

        LocalToken local_token = new LocalToken();

        local_token.uuid = token.uuid;
        local_token.email = token.email;
        local_token.created_at = new java.sql.Date(token.creationTime.getMillis());
        local_token.expire_at = new java.sql.Date(token.expirationTime.getMillis());
        local_token.is_sign_up = token.isSignUp;

        local_token.save();

        logger.debug(String.format("doSaveToken uuid:%s", local_token.uuid));
    }

    @Override
    public Identity doFind(IdentityId identityId) {
        logger.debug(String.format("doFind userId:%s", identityId.userId()));

        LocalUser local_user = LocalUser.find.byId(identityId.userId());

        if (local_user == null) {
            logger.debug("doFind LocalUser not found");
            return null;
        }

        SocialUser social_user = createSocialUserFrom(local_user);

        return social_user;
    }

    @Override
    public Token doFindToken(String tokenId) {
        logger.debug(String.format("doFindToken tokenId:%s", tokenId));

        LocalToken local_token = LocalToken.find.byId(tokenId);

        if (local_token == null) {
            logger.debug("doFindToken LocalToken not found");
            return null;
        }

        Token result = new Token();
        result.uuid = local_token.uuid;
        result.email = local_token.email;
        result.isSignUp = local_token.is_sign_up;
        result.creationTime = new DateTime(local_token.created_at);
        result.expirationTime = new DateTime(local_token.expire_at);

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

        SocialUser social_user = createSocialUserFrom(list.get(0));

        return social_user;
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
        logger.debug("doDeleteExpiredTokens");

        List<LocalToken> list = LocalToken.find.where().lt("expire_at", new DateTime().toString()).findList();

        for(LocalToken localToken : list) {
            localToken.delete();
        }
    }

    private void copyIdentityToLocalUser(Identity source, LocalUser dest) {
        dest.id = source.identityId().userId();
        dest.provider = source.identityId().providerId();
        dest.first_name = source.firstName();
        dest.last_name = source.lastName();
        dest.email = source.email().get();
        dest.password = source.passwordInfo().get().password();
    }

    private SocialUser createSocialUserFrom(LocalUser local_user) {
        SocialUser social_user = new SocialUser(
                new IdentityId(local_user.id, local_user.provider)
              , local_user.first_name
              , local_user.last_name
              , String.format("%s %s", local_user.first_name, local_user.last_name)
              , Option.apply(local_user.email)
              , null
              , new AuthenticationMethod("userPassword")
              , null
              , null
              , Some.apply(new PasswordInfo("bcrypt", local_user.password, null))
          );

        return social_user;
    }
}
