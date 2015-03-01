package utils.controller.errors.client;

public class ResourceAlreadyExistsError extends BasicError {
    private static final String MESSAGE = "Resource already exists";

    public ResourceAlreadyExistsError() {
        super(MESSAGE);
    }
}
