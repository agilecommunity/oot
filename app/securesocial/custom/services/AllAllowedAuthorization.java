package securesocial.custom.services;

import securesocial.core.java.Authorization;

public class AllAllowedAuthorization<U> implements Authorization<U> {
    @Override
    public boolean isAuthorized(U user, String[] params) {
        return true;
    }
}