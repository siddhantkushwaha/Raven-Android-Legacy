package com.siddhantkushwaha.raven.utility.intentmanager;

import android.app.Activity;
import android.content.Intent;

import com.siddhantkushwaha.raven.activity.HomeActivity;

public class HomeActivityIntentManager {

    public static void openActivity(Activity activity, Boolean finish) {

        Intent intent = new Intent(activity, HomeActivity.class);
//        intent.putExtra();
        activity.startActivity(intent);

        if (finish)
            activity.finish();
    }
}
