package utils.controller;

import utils.controller.errors.client.ValidationError;
import play.api.mvc.Codec;
import play.libs.Json;

public class Results {

    static Codec utf8 = Codec.javaSupported("utf-8");

    public static play.mvc.Results.Status validationError(com.fasterxml.jackson.databind.JsonNode errors) {
        return new play.mvc.Results.Status(play.core.j.JavaResults.UnprocessableEntity(), Json.toJson(new ValidationError(errors)) , utf8);
    }

    public static play.mvc.Results.Status validationError(String errors) {
        return new play.mvc.Results.Status(play.core.j.JavaResults.UnprocessableEntity(), Json.toJson(new ValidationError(errors)) , utf8);
    }

    public static play.mvc.Results.Status insufficientPermissionsError(String message) {
        return play.mvc.Results.unauthorized(Json.toJson(new utils.controller.errors.client.InsufficientPermissionsError(message)));
    }

    public static play.mvc.Results.Status resourceAlreadyExistsError() {
        return new play.mvc.Results.Status(play.core.j.JavaResults.Conflict(), Json.toJson(new utils.controller.errors.client.ResourceAlreadyExistsError()), utf8);
    }

    public static play.mvc.Results.Status resourceNotFoundError() {
        return play.mvc.Results.notFound(Json.toJson(new utils.controller.errors.client.ResourceNotFoundError()));
    }

    public static play.mvc.Results.Status faildToParseQueryStringError() {
        return play.mvc.Results.internalServerError(Json.toJson(new utils.controller.errors.server.FailedToParseQueryStringsError()));
    }

    public static play.mvc.Results.Status badRequestError(String message) {
        return play.mvc.Results.badRequest(Json.toJson(new utils.controller.errors.client.BasicError(message)));
    }

    public static play.mvc.Results.Status notFoundError(String message) {
        return play.mvc.Results.notFound(Json.toJson(new utils.controller.errors.client.BasicError(message)));
    }

    public static play.mvc.Results.Status internalServerError(String message) {
        return play.mvc.Results.internalServerError(Json.toJson(new utils.controller.errors.server.BasicError(message)));
    }
}
