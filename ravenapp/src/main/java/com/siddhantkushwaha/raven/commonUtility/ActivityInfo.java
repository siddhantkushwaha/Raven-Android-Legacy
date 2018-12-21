package com.siddhantkushwaha.raven.commonUtility;

import android.os.Bundle;

public class ActivityInfo {

    private static String className;
    private static Bundle intentInfo;

    public static String getClassName() {
        return className;
    }

    public static Bundle getIntentInfo() {
        return intentInfo;
    }

    public static void setActivityInfo(String className, Bundle activityInfo) {
        ActivityInfo.className = className;
        ActivityInfo.intentInfo = activityInfo;
    }
}
