package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import filters.RequireCSRFCheck4Ng;
import models.LocalUser;
import models.MenuItem;
import models.MenuItemImage;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.io.*;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import securesocial.custom.MySecuredAction;
import utils.controller.Results;

public class MenuItems extends WithSecureSocialController {

    private static Logger.ALogger logger = Logger.of("application.controllers.MenuItems");

    private static class StatusParameter {
        public String value = "";

        public StatusParameter(String value) {
            if (value == null) {
                return;
            }
            this.value = value;
        }
    }

    private static class Parameters {
        public StatusParameter status = null;

        public Parameters(Http.Request request) {
            String value = request.getQueryString("status");
            if (value != null && !value.isEmpty()) {
                this.status = new StatusParameter(value);
                return;
            }
        }
    }

    @MySecuredAction
    public static Result index() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        Parameters parameters = null;
        parameters = new Parameters(request());

        ExpressionList<MenuItem> menus = MenuItem.find.where();

        if (parameters.status != null) {
            menus.eq("status", parameters.status.value);
        }

        return ok(Json.toJson(menus.orderBy("id").findList()));
    }

    @MySecuredAction
    public static Result indexByShopName(String shopName) {
        response().setHeader(CACHE_CONTROL, "no-cache");

        logger.debug(String.format("#indexByShopName shopName: %s", shopName));

        Parameters parameters = null;
        parameters = new Parameters(request());

        ExpressionList<MenuItem> menus = MenuItem.find.where();

        if (parameters.status != null) {
            logger.debug("#indexByShopName filter enabled status: {}", parameters.status.value);
            menus.eq("status", parameters.status.value);
        }

        return ok(Json.toJson(menus.eq("shop_name", shopName).orderBy("id").findList()));
    }

    //@RequireCSRFCheck4Ng  /* IE8でjquery file uploadを使うとヘッダにX-Requested-Withが使えないため設定しない、byPassも無理らしい */
    @MySecuredAction
    public static Result create() {
        response().setHeader(CACHE_CONTROL, "no-cache");

        String contentType = request().getHeader("Content-Type");

        logger.debug("#create Content-Type: {}", contentType);

        LocalUser currentUser = getCurrentUser();

        if (!currentUser.isAdmin) {
            logger.warn("#create only admin can create menu. currentUser.id: {}", currentUser.id);
            return Results.insufficientPermissionsError("Current user can't create menu item.");
        }

        if (contentType != null && contentType.contains("text/plain")) {
            // text/plainの場合はCSV文字列で指定したとみなす
            logger.debug("#create build from csv");
            try {
                createFromCsv(request().body().asText());
            } catch (IOException e) {
                return Results.internalServerError(e.getLocalizedMessage());
            }
        } else if (contentType != null && contentType.contains("multipart/form-data;")) {
            // multipart/form-dataの場合はCSVファイルで指定したとみなす
            logger.debug("#create build form multipart form-data");
            try {
                createFromFile(request().body().asMultipartFormData());
            } catch (IOException e) {
                return Results.internalServerError(e.getLocalizedMessage());
            }
        } else {
            // どれでもない場合はJsonで指定したとみなす
            logger.debug("#create build from json");

            // ここだけ特殊で、結果をそのまま返す
            return createFromJson(request().body().asJson());
        }

        return ok();
    }

    @RequireCSRFCheck4Ng
    @MySecuredAction
    public static Result update(Long id) {
        logger.debug("#update id: {}", id);

        LocalUser currentUser = getCurrentUser();

        if (!currentUser.isAdmin) {
            logger.warn("#update only admin can create menu. currentUser.id: {}", currentUser.id);
            return Results.insufficientPermissionsError("Current user can't update menu item.");
        }

        JsonNode json = request().body().asJson();

        String base64Image = extractImage(json); // 長すぎる文字列があると Form.bind が失敗するようなのでここで抽出しておく

        logger.debug("#update json: {}", json);
        logger.debug("#update hasImage: {}", (base64Image != null && base64Image.length() > 0));

        Form<MenuItem> filledForm = Form.form(MenuItem.class).bind(json);

        if (filledForm.hasErrors()) {
            logger.debug("#update item has error. errors: {}", filledForm.errorsAsJson());
            return Results.validationError(filledForm.errorsAsJson());
        }

        MenuItem item = filledForm.get();

        try {
            MenuItems.saveImage(item, base64Image);
        } catch (IOException ex) {
            logger.error("#update save image", ex);
            return Results.internalServerError("画像の保存に失敗しました");
        }

        item.update();

        return ok(Json.toJson(item));
    }

    @RequireCSRFCheck4Ng
    @MySecuredAction
    public static Result delete(Long id) {
        logger.debug("#delete id: {}", id);

        return new Todo();
    }

    /**
     * Json文字列からMenuItemを生成する
     * @param json
     */
    private static Result createFromJson(JsonNode json) {

        logger.debug("#createFromJson json: {}", json);

        StringBuilder result = new StringBuilder();
        result.append("[");

        Ebean.beginTransaction();

        boolean hasError = false;

        try {
            for (int index = 0; index < json.size(); index++) {
                if (index != 0) {
                    result.append(",");
                }

                JsonNode current = json.get(index);

                String base64Image = extractImage(current); // 長すぎる文字列があると Form.bind が失敗するようなのでここで抽出しておく

                Form<MenuItem> filledForm = Form.form(MenuItem.class).bind(current);

                if (filledForm.hasErrors()) {
                    logger.debug("#createFromJson item has error. errors: {}", filledForm.errorsAsJson());
                    result.append(filledForm.errorsAsJson().toString());
                    hasError = true;
                    continue;
                }

                MenuItem item = filledForm.get();

                item.save();

                try {
                    MenuItems.saveImage(item, base64Image);
                    item.update();
                } catch (IOException ex) {
                    logger.error("#createFromJson save image", ex);
                    throw ex;
                }

                result.append(Json.toJson(item).toString());
            }

            if (!hasError) {
                Ebean.commitTransaction();
            } else {
                Ebean.rollbackTransaction();
            }
        } catch (Exception ex) {
            logger.error("#createFromJson save error", ex);

            Ebean.rollbackTransaction();
        }

        result.append("]");

        logger.debug("#createFromJson result: {}", result.toString());

        if (!hasError) {
            return ok(result.toString());
        } else {
            return Results.validationError(result.toString());
        }
    }

    private static void createFromCsv(String text) throws IOException {

        logger.debug("#createFromCsv text: {}", text);

        if (text == null || text.isEmpty()) {
            logger.debug("#createFromCsv text is emtpy or null.");
            return;
        }

        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader().withQuoteChar('"');
        CsvMapper csvMapper = new CsvMapper();

        MappingIterator<Map<String, Object>> iterator = csvMapper.reader(Map.class).with(bootstrap).readValues(text);

        List<Map<String, Object>> items = iterator.readAll();

        for (Map<String, Object> item : items) {
            if ("".equals(item.get("fixedOnPurchaseExcTax"))) {
                item.remove("fixedOnPurchaseExcTax");
            }

            if ("".equals(item.get("fixedOnPurchaseIncTax"))) {
                item.remove("fixedOnPurchaseIncTax");
            }
        }

        createFromJson(Json.toJson(items));
    }

    private static void createFromFile(Http.MultipartFormData formData) throws IOException {

        if (formData.getFiles().size() == 0) {
            logger.debug("#createFromFile no files");
            return;
        }

        for (Http.MultipartFormData.FilePart filePart : formData.getFiles()) {
            logger.debug("#createFromFile files key: {} fileName: {} contentType: {}", filePart.getKey(), filePart.getFilename(), filePart.getContentType());
        }

        File csvFile = formData.getFile("menuItems").getFile();

        logger.debug("#createFromFile fileName: {}", csvFile.getName());

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

    private static void saveImage(MenuItem item, String base64Image) throws IOException {
        logger.debug("#saveImage");

        if (base64Image == null || base64Image.length() == 0 ) {
            logger.debug("#saveImage image null");
            return;
        }

        try {
            MenuItemImage image = new MenuItemImage(item);
            image.setImageBase64(base64Image);
            image.save();
        } catch (IOException ex) {
            logger.error("#saveImage save image", ex);
            throw ex;
        }
    }

    private static String extractImage(JsonNode node) {
        String base64Image = "";

        if (node.has("image")) {
            ObjectNode jsonObject = (ObjectNode)node;
            base64Image = node.get("image").asText();
            jsonObject.remove("image");
            node = jsonObject;
        }

        return base64Image;
    }
}
