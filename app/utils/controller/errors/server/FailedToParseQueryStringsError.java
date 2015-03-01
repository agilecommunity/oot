package utils.controller.errors.server;

public class FailedToParseQueryStringsError extends BasicError {
    private static final String MESSAGE = "Failed to parse query strings";

    public FailedToParseQueryStringsError() {
        super(MESSAGE);
    }
}
