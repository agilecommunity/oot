package controllers;

import java.text.ParseException;
import java.util.List;

import models.DailyOrder;
import models.LocalUser;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.controller.ParameterConverter;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;

import filters.RequireCSRFCheck4Ng;

public class DailyOrders extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.DailyOrders");

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showByOrderDate(String orderDateStr) {

        response().setHeader(CACHE_CONTROL, "no-cache");

        java.sql.Date orderDate;
        try {
            orderDate = ParameterConverter.convertDateFrom(orderDateStr);
        } catch (ParseException e) {
            logger.debug(String.format("#showByOrderDate parse error orderDateStr: %s", orderDateStr));
            return badRequest();
        }

        List<DailyOrder> list = DailyOrder.findBy(orderDate);

        if (list == null || list.size() == 0) {
            logger.debug(String.format("#showByOrderDate order not found orderDateStr:%s", orderDateStr));
            return notFound();
        }

        return ok(Json.toJson(list));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result showMine() {

        response().setHeader(CACHE_CONTROL, "no-cache");

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        ExpressionList<DailyOrder> query = DailyOrder.find.where().eq("user_id", user.identityId().userId());


        if (request().queryString().containsKey("orderDate")) {
            query.eq("orderDate", request().queryString().get("orderDate"));
        }

        List<DailyOrder> orders = query.findList();

        return ok(Json.toJson(orders));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result create() {

        logger.debug("#create");

        response().setHeader(CACHE_CONTROL, "no-cache");

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#create request-body:%s", request().body().toString()));

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("#create object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyOrder object = filledForm.get();

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (!DailyOrders.canEdit(object, user)) {
            logger.warn(String.format("#create cant create localUser.id:%s identity.user.id:%s", object.localUser.id, user.identityId().userId() ));
            return badRequest();
        }

        if (DailyOrder.findBy(object.orderDate, object.localUser.id) != null) {
            logger.debug("#create object already exists");
            return badRequest();
        }

        logger.debug(String.format("#create order.localUser.id:%s", object.localUser.id));
        logger.debug(String.format("#create order.orderDate:%s", object.orderDate));

        object.save();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    @BodyParser.Of(play.mvc.BodyParser.Json.class)
    public static Result update(Long id) {
        logger.debug("#update");

        if (DailyOrder.find.byId(id) == null) {
            logger.debug("#update object doesnt exist");
            return badRequest();
        }

        JsonNode json = request().body().asJson();

        logger.debug(String.format("#update request-body:%s", request().body().toString()));

        Form<DailyOrder> filledForm = Form.form(DailyOrder.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.warn(String.format("#update object has some errors. %s", filledForm.errorsAsJson().toString()));
            return badRequest(filledForm.errorsAsJson().toString());
        }

        DailyOrder object = filledForm.get();

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (! DailyOrders.canEdit(object, user)) {
            logger.warn(String.format("#update cant update localUser.id:%s identity.user.id:%s", object.localUser.id, user.identityId().userId() ));
            return badRequest();
        }

        logger.debug(String.format("#update order.localUser.id:%s", object.localUser.id));
        logger.debug(String.format("#update order.orderDate:%s", object.orderDate));

        object.update();

        return ok(Json.toJson(object));
    }

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result delete(Long id) {

        logger.debug(String.format("#delete id: %s", id));

        response().setHeader(CACHE_CONTROL, "no-cache");

        DailyOrder object = DailyOrder.find.byId(id);

        if (object == null) {
            logger.debug("#delete object not found");
            return ok();
        }

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);

        if (! DailyOrders.canEdit(object, user)) {
            logger.warn(String.format("#delete cant delete others order localUser.id:%s identity.user.id:%s", object.localUser.id, user.identityId().userId() ));
            return badRequest();
        }

        object.delete();

        return ok();
    }

    private static boolean canEdit(DailyOrder order, Identity user) {

        logger.debug(String.format("#canEdit identityId: %s", user.identityId()));
        logger.debug(String.format("#canEdit identityId.userId: %s", user.identityId().userId()));

        LocalUser currentUser = LocalUser.find.where().eq("id", user.identityId().userId()).findUnique();

        // 管理者である
        if (currentUser.isAdmin == true) {
            return true;
        }

        // オブジェクトの所有者である
        if (order.localUser.id.equals(user.identityId().userId())) {
            return true;
        }

        return false;
    }

}
