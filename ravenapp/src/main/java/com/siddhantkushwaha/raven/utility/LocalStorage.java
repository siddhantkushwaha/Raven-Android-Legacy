package com.siddhantkushwaha.raven.utility;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class LocalStorage {

    public static void set(Context context, String bucketName, String key, String data) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(bucketName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();
    }

    public static String get(Context context, String bucketName, String key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(bucketName, MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
}