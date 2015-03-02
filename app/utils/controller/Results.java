package utils.controller;

import utils.controller.errors.client.ValidationError;
import play.api.mvc.Codec;
import play.libs.Json;

public class Results {

    static Codec utf8 = Codec.javaSupported("utf-8");

    /**
     * メニューの締め切り後に注文をしようとした場合に返すエラー
     * @return
     */
    public static play.mvc.Results.Status menuHasExpiredError() {
        return new play.mvc.Results.Status(play.core.j.JavaResults.Forbidden(), Json.toJson(new utils.controller.errors.client.MenuHasExpiredError()) , utf8);
    }

    /**
     * 指定したURLに誤りがある場合に返すエラー
     * @param errors
     * @return
     */
    public static play.mvc.Results.Status invalidLinkError(String errors) {
        return new play.mvc.Results.Status(play.core.j.JavaResults.Forbidden(), Json.toJson(new utils.controller.errors.client.BasicError(errors)) , utf8);
    }

    /**
     * オブジェクトのバリデーションで問題があった場合に返すエラー
     * @param errors
     * @return
     */
    public static play.mvc.Results.Status validationError(com.fasterxml.jackson.databind.JsonNode errors) {
        return new play.mvc.Results.Status(play.core.j.JavaResults.UnprocessableEntity(), Json.toJson(new ValidationError(errors)) , utf8);
    }

    /**
     * オブジェクトのバリデーションで問題があった場合に返すエラー
     * @param errors
     * @return
     */
    public static play.mvc.Results.Status validationError(String errors) {
        return new play.mvc.Results.Status(play.core.j.JavaResults.UnprocessableEntity(), Json.toJson(new ValidationError(errors)) , utf8);
    }

    /**
     * 操作するための権限が不足している場合に返すエラー
     * @param message
     * @return
     */
    public static play.mvc.Results.Status insufficientPermissionsError(String message) {
        return play.mvc.Results.unauthorized(Json.toJson(new utils.controller.errors.client.InsufficientPermissionsError(message)));
    }

    /**
     * オブジェクトがすでに存在する(のに作成しようとした)場合に返すエラー
     * @return
     */
    public static play.mvc.Results.Status resourceAlreadyExistsError() {
        return new play.mvc.Results.Status(play.core.j.JavaResults.Conflict(), Json.toJson(new utils.controller.errors.client.ResourceAlreadyExistsError()), utf8);
    }

    /**
     * オブジェクトが存在しない(のに操作しようとした)場合に返すエラー
     * @return
     */
    public static play.mvc.Results.Status resourceNotFoundError() {
        return play.mvc.Results.notFound(Json.toJson(new utils.controller.errors.client.ResourceNotFoundError()));
    }

    /**
     * QueryStringの解析に失敗した場合に返すエラー
     * これだけ細かすぎ??
     * @return
     */
    public static play.mvc.Results.Status faildToParseQueryStringError() {
        return play.mvc.Results.internalServerError(Json.toJson(new utils.controller.errors.server.FailedToParseQueryStringsError()));
    }

    /**
     * 上記に当たらないもので、BadRequest(400)の場合に返すエラー
     * @param message
     * @return
     */
    public static play.mvc.Results.Status badRequestError(String message) {
        return play.mvc.Results.badRequest(Json.toJson(new utils.controller.errors.client.BasicError(message)));
    }

    /**
     * 上記に当たらないもので、NotFound(404)の場合に返すエラー
     * @param message
     * @return
     */
    public static play.mvc.Results.Status notFoundError(String message) {
        return play.mvc.Results.notFound(Json.toJson(new utils.controller.errors.client.BasicError(message)));
    }

    /**
     * 上記に当たらないもので、InternalServerError(500)の場合に返すエラー
     * @param message
     * @return
     */
    public static play.mvc.Results.Status internalServerError(String message) {
        return play.mvc.Results.internalServerError(Json.toJson(new utils.controller.errors.server.BasicError(message)));
    }
}
