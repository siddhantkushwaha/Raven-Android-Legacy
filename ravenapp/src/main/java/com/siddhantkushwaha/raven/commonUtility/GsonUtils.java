package com.siddhantkushwaha.raven.commonUtility;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class GsonUtils {

    private static final Gson gson = new Gson();

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
