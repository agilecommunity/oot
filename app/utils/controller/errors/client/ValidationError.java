package utils.controller.errors.client;

import play.libs.Json;

public class ValidationError extends BasicError {

    private static final String MESSAGE = "Validation Failed";

    public com.fasterxml.jackson.databind.JsonNode errors;

    public ValidationError(com.fasterxml.jackson.databind.JsonNode errors) {
        super(MESSAGE);
        this.errors = errors;
    }

    public ValidationError(String errors) {
        super(MESSAGE);
        this.errors = Json.parse(errors);
    }
}
