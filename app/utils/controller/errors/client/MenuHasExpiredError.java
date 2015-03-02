package utils.controller.errors.client;

public class MenuHasExpiredError extends BasicError {
    private static final String MESSAGE = "Menu has expired";

    public MenuHasExpiredError() {
        super(MESSAGE);
    }
}
