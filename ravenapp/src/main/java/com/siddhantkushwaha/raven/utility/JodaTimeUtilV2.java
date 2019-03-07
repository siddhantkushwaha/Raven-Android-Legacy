package com.siddhantkushwaha.raven.utility;

import com.google.firebase.Timestamp;
import com.siddhantkushwaha.nuttertools.JodaTimeUtil;

import org.joda.time.DateTime;

import androidx.annotation.Nullable;


public class JodaTimeUtilV2 extends JodaTimeUtil {

    public static DateTime getDateTime(@Nullable Timestamp timestamp) {

        if (timestamp == null)
            return null;
        return new DateTime(timestamp.toDate());
    }

    public static String getDateTimeAsString(@Nullable Timestamp timestamp) {

        if (timestamp == null)
            return null;

        return new DateTime(timestamp.toDate()).toString();
    }
}
