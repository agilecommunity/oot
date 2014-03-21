package utils.gson;

import java.lang.reflect.Type;

import models.DailyOrder;
import models.LocalUser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class OotGsonGenerator {

    public static Gson create() {

        GsonBuilder builder = OotGsonGenerator.builderForBasicClasses();

        return builder
            .registerTypeAdapter(models.DailyOrder.class, new JsonDeserializer<models.DailyOrder>(){
                @Override
                public DailyOrder deserialize(JsonElement json, Type typeOfT
                        , JsonDeserializationContext context) throws JsonParseException {

                    DailyOrder order = OotGsonGenerator.builderForBasicClasses().create().fromJson(json, DailyOrder.class);

                    if (order.local_user != null && order.local_user.id != null) {
                        if (LocalUser.find.byId(order.local_user.id) == null ) {
                            order.local_user = null;
                        }
                    }

                    return order;
                }

            }).create();

    }

    private static GsonBuilder builderForBasicClasses() {

        GsonBuilder builder = new GsonBuilder();

        return builder
            .registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
                @Override
                public java.util.Date deserialize(com.google.gson.JsonElement p1, java.lang.reflect.Type p2,
                                            com.google.gson.JsonDeserializationContext p3) {

                    java.util.Date deserialized = null;
                    try {
                        deserialized = new java.util.Date(p1.getAsLong());
                    } catch ( java.lang.NumberFormatException ex) {
                        deserialized = null;
                    }

                    return deserialized;
                }
            });
    }

}
