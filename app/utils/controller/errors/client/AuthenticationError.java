package utils.controller.errors.client;

public class AuthenticationError extends BasicError {

    private static final String MESSAGE = "Must specify authentication token";

    public com.fasterxml.jackson.databind.JsonNode errors;

    public AuthenticationError() {
        super(MESSAGE);
    }
}
