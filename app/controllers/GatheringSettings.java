package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.GatheringSetting;
import models.LocalUser;
import org.joda.time.DateTime;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import securesocial.custom.MySecuredAction;
import utils.controller.Results;

public class GatheringSettings extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.GatheringSettings");

    @MySecuredAction
    public static Result get() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        GatheringSetting item = GatheringSetting.find.findUnique();

        logger.debug("#get json:{}", Json.toJson(item));

        return ok(Json.toJson(item));
    }

    @MySecuredAction
    public static Result update() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        String contentType = request().getHeader("Content-Type");

        logger.debug("#update Content-Type: {}", contentType);

        LocalUser currentUser = getCurrentUser();

        if (!currentUser.isAdmin) {
            logger.warn("#update only admin can update gathering setting. currentUser.id: {}", currentUser.email);
            return Results.insufficientPermissionsError("Current user can't update.");
        }

        JsonNode json = request().body().asJson();

        logger.debug("#update json: {}", json);

        GatheringSetting dbItem = GatheringSetting.find.findUnique();

        Form<GatheringSetting> filledForm = Form.form(GatheringSetting.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#update item has error. errors: {}", filledForm.errorsAsJson());
            return Results.validationError(filledForm.errorsAsJson());
        }

        GatheringSetting item = filledForm.get();

        if (dbItem.id != item.id) {
            logger.debug("#update request id isnot valid. requestId: {} dbId: {}", item.id, dbItem.id);
            return Results.badRequestError("Id is not valid");
        }

        item.createdAt = dbItem.createdAt;
        item.updatedAt = DateTime.now();  // UpdatedTimestampが使えないので、ここで更新する Ebeanのバージョンあげられたら使えるはず…
        item.createdBy = dbItem.createdBy;
        item.updatedBy = currentUser.email;
        item.update();

        return ok(Json.toJson(item));
    }
}
