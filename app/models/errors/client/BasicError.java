package models.errors.client;

import ch.qos.logback.core.net.server.Client;

public class BasicError {
    public String message;

    public BasicError(String message) {
        this.message = message;
    }
}
