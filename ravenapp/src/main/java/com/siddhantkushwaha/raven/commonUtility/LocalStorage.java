package com.siddhantkushwaha.raven.commonUtility;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class LocalStorage {

    public static void setJson(Context context, String bucketName, String key, String json) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(bucketName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, json);
        editor.apply();
    }

    public static String getJson(Context context, String bucketName, String key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(bucketName, MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
}