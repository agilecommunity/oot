package utils.securesocial;

import models.LocalUser;
import play.libs.Scala;
import scala.Option;
import scala.Some;
import securesocial.core.AuthenticationMethod;
import securesocial.core.BasicProfile;
import securesocial.core.PasswordInfo;
import securesocial.core.java.SecureSocial;
import securesocial.core.providers.UsernamePasswordProvider$;
import securesocial.core.providers.utils.PasswordHasher;
import securesocial.core.providers.utils.PasswordHasher$;

public class BasicProfileBasedOperations {

    public static String createFullName(String firstName, String lastName) {
        return String.format("%s %s", firstName, lastName);
    }

    public static BasicProfile createProfile(LocalUser localUser, AuthenticationMethod authMethod) {

        String fullName = BasicProfileBasedOperations.createFullName(localUser.firstName, localUser.lastName);

        BasicProfile basicProfile = new BasicProfile(
                localUser.provider
                , localUser.id
                , Option.apply(localUser.firstName)
                , Option.apply(localUser.lastName)
                , Option.apply(fullName)
                , Option.apply(localUser.email)
                , null
                , authMethod
                , null
                , null
                , Some.apply(new PasswordInfo(PasswordHasher$.MODULE$.id(), localUser.password, null))
        );

        return basicProfile;
    }

    public static BasicProfile createProfile(String providerId, String id, String firstName, String lastName, String fullName,
                                             String email, AuthenticationMethod authMethod, String password) {

        PasswordHasher defaultHasher = new PasswordHasher.Default();
        return new BasicProfile(
                providerId,
                id,
                Scala.Option(firstName),
                Scala.Option(lastName),
                Scala.Option(fullName),
                Scala.Option(email),
                null,
                authMethod,
                null,
                null,
                Scala.Option(defaultHasher.hash(password))
        );
    }

}
