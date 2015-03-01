package utils.controller.errors.client;

public class InsufficientPermissionsError extends BasicError {

    public com.fasterxml.jackson.databind.JsonNode errors;

    public InsufficientPermissionsError(String message) {
        super(message);
    }
}
