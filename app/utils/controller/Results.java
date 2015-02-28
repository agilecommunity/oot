package utils.controller;

import models.errors.client.ValidationError;
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
        return play.mvc.Results.unauthorized(Json.toJson(new models.errors.client.InsufficientPermissionsError(message)));
    }

    public static play.mvc.Results.Status internalServerError(String message) {
        return play.mvc.Results.internalServerError(Json.toJson(new models.errors.server.BasicError(message)));
    }
}
