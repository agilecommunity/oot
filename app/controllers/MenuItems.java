package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import models.LocalUser;
import models.MenuItem;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import java.io.*;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class MenuItems extends Controller {

    private static Logger.ALogger logger = Logger.of("application.controllers.MenuItems");

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        List<MenuItem> menus = MenuItem.find.orderBy("id").findList();

        return ok(Json.toJson(menus));
    }

    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result indexByShopName(String shopName) {
        response().setHeader(CACHE_CONTROL, "no-cache");

        logger.debug(String.format("#indexByShopName shopName: %s", shopName));

        List<MenuItem> menus = MenuItem.find.where().eq("shop_name", shopName).orderBy("id").findList();

        logger.debug(String.format("#indexByShopName count: %d", menus.size()));

        return ok(Json.toJson(menus));
    }

    //@RequireCSRFCheck4Ng  /* IE8でjquery file uploadを使うとヘッダにX-Requested-Withが使えないため、byPassも無理らしい */
    @SecureSocial.SecuredAction(ajaxCall = true)
    public static Result create() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        String contentType = request().getHeader("Content-Type");

        logger.debug(String.format("#create Content-Type: %s", contentType));

        Identity user = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        LocalUser localUser = LocalUser.find.byId(user.email().get());

        if (!localUser.is_admin) {
            logger.warn(String.format("#create only admin can create menu. local_user.id:%s", localUser.email));
            return unauthorized();
        }

        if (contentType != null && contentType.contains("text/plain")) {
            logger.debug("#create build from csv");
            try {
                createFromCsv(request().body().asText());
            } catch (IOException e) {
                return internalServerError(String.format("{error: \"%s\"", e.getLocalizedMessage()));
            }
        } else if (contentType != null && contentType.contains("multipart/form-data;")) {
            logger.debug("#create build form multipart form-data");
            try {
                createFromFile(request().body().asMultipartFormData());
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
                logger.debug(String.format("#create item(%d) has error. errors: %s", index + 1, filledForm.errorsAsJson()));
                continue;
            }

            MenuItem item = filledForm.get();

            if (item.code == null) {
                item.code = "";
            }

            item.save();
        }
    }

    private static void createFromCsv(String text) throws IOException {

        logger.debug(String.format("#createFromCsv text:%s", text));

        if (text == null || text.isEmpty()) {
            logger.debug("#createFromCsv text is emtpy or null.");
            return;
        }

        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader().withQuoteChar('"');
        CsvMapper csvMapper = new CsvMapper();

        MappingIterator<Map<String, Object>> iterator = csvMapper.reader(Map.class).with(bootstrap).readValues(text);

        List<Map<String, Object>> items = iterator.readAll();

        System.out.println(items);

        createFromJson(Json.toJson(items));
    }

    private static void createFromFile(Http.MultipartFormData formData) throws IOException {

        if (formData.getFiles().size() == 0) {
            logger.debug("#createFromFile no files");
            return;
        }

        for (Http.MultipartFormData.FilePart filePart : formData.getFiles()) {
            logger.debug(String.format("#createFromFile files key:%s fileName:%s contentType:%s", filePart.getKey(), filePart.getFilename(), filePart.getContentType()));
        }

        File csvFile = formData.getFile("menuItems").getFile();

        logger.debug(String.format("#createFromFile fileName:%s", csvFile.getName()));

        BufferedReader bCsvFile;
        try {
            FileInputStream fsCsvFile = new FileInputStream(csvFile);
            InputStreamReader isCsvFile = new InputStreamReader(fsCsvFile, "UTF-8");
            bCsvFile = new BufferedReader(isCsvFile);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (UnsupportedEncodingException e) {
            throw e;
        }

        StringBuffer csvText = new StringBuffer();

        try {
            String str = bCsvFile.readLine();
            while (str != null) {
                csvText.append(str + "\r\n");
                str = bCsvFile.readLine();
            }
        } catch (IOException e) {
            throw e;
        }

        createFromCsv(csvText.toString());
    }
}
