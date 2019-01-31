package com.siddhantkushwaha.raven;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class Raven extends Application {

    private static final String TAG = "Raven";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Application Started.");

        Fabric.with(this, new Crashlytics());
    }
}
