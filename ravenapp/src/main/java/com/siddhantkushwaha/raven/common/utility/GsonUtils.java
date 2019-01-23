package com.siddhantkushwaha.raven.common.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonUtils {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static String toGson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromGson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    public static <T> T fromGson(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
