package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import filters.RequireCSRFCheck4Ng;
import models.DailyMenu;
import models.MenuItem;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.java.SecureSocial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
public class MenuItems extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.MenuItems");

    @RequireCSRFCheck4Ng()
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        List<MenuItem> menus = MenuItem.find.orderBy("id").findList();

        return ok(Json.toJson(menus));
    }

    @RequireCSRFCheck4Ng
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result create() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        String contentType = request().getHeader("Content-Type");

        logger.debug(String.format("#create Content-Type: %s", contentType));

        if (contentType != null && contentType.contains("text/plain")) {
            logger.debug("#create build from csv");
            try {
                createFromCsv(request().body().asText());
            } catch (IOException e) {
                return internalServerError(String.format("{error: \"%s\"", e.getLocalizedMessage()));
            }
        } else {
            logger.debug("#create build from json");
            createFromJson(request().body().asJson());
        }

        return ok();
    }

    private static void createFromJson(JsonNode json) {

        logger.debug(String.format("#create json:%s", json));

        for (int index=0; index<json.size(); index++) {
            logger.debug(String.format("#create item(%d) : %s", index + 1, json.get(index)));

            Form<MenuItem> filledForm = Form.form(MenuItem.class).bind(json.get(index));

            if (filledForm.hasErrors()) {
                logger.debug(String.format("#create item(%d) has error. errors: %s", filledForm.errorsAsJson()));
                continue;
            }

            MenuItem item = filledForm.get();
            item.save();
        }
    }

    private static void createFromCsv(String text) throws IOException {

        logger.debug(String.format("#createFromCsv text:%s", text));

        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader().withQuoteChar('"');
        CsvMapper csvMapper = new CsvMapper();

        MappingIterator<Map<String, Object>> iterator = csvMapper.reader(Map.class).with(bootstrap).readValues(text);

        List<Map<String, Object>> items = iterator.readAll();

        System.out.println(items);

        createFromJson(Json.toJson(items));
    }
}
