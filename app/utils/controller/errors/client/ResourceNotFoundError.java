package utils.controller.errors.client;

public class ResourceNotFoundError extends BasicError {
    private static final String MESSAGE = "Resource not found";

    public ResourceNotFoundError() {
        super(MESSAGE);
    }
}
