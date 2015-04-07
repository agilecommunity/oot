package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import models.GatheringSetting;
import models.LocalUser;
import models.MenuItem;
import org.joda.time.DateTime;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.snakeyaml.YamlUtil;

import java.util.List;

public class GatheringSettings extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.GatheringSettings");

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result get() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        GatheringSetting item = GatheringSetting.find.findUnique();

        return ok(Json.toJson(item));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result update() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        String contentType = request().getHeader("Content-Type");

        logger.debug(String.format("#update Content-Type: %s", contentType));

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.isAdmin) {
            logger.warn(String.format("#update only admin can update gathering setting. localUser.id:%s", localUser.email));
            return utils.controller.Results.insufficientPermissionsError("Current user can't update.");
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#update json:%s", json));

        GatheringSetting dbItem = GatheringSetting.find.findUnique();

        Form<GatheringSetting> filledForm = Form.form(GatheringSetting.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug(String.format("#update item has error. errors: %s", filledForm.errorsAsJson()));
            return utils.controller.Results.validationError(filledForm.errorsAsJson());
        }

        GatheringSetting item = filledForm.get();

        if (dbItem.id != item.id) {
            logger.debug(String.format("#update request id isnot valid. requestId:{} dbId:{}", item.id, dbItem.id));
            return utils.controller.Results.badRequestError("Id is not valid");
        }

        item.createdAt = dbItem.createdAt;
        item.updatedAt = DateTime.now();  // UpdatedTimestampが使えないので、ここで更新する Ebeanのバージョンあげられたら使えるはず…
        item.createdBy = dbItem.createdBy;
        item.updatedBy = localUser.email;
        item.update();

        return ok(Json.toJson(item));
    }
}
