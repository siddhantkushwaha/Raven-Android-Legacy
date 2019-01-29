package com.siddhantkushwaha.raven.utility;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.siddhantkushwaha.nuttertools.JodaTimeUtil;

import org.joda.time.DateTime;


public class JodaTimeUtilV2 extends JodaTimeUtil {

    public static DateTime getJodaDateTimeFromFirebaseTimestamp(Timestamp timestamp2) {

        try {
            return new DateTime(timestamp2.toDate());
        } catch (Exception e) {
            Log.e(JodaTimeUtilV2.class.toString(), e.toString());
        }

        return null;
    }
}
