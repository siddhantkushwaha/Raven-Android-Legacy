package com.siddhantkushwaha.raven.commonUtility;

import android.util.Log;

import com.google.firebase.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Date;


public class DateTimeUtils {

    public static boolean isToday(DateTime time) {
        return LocalDate.now().compareTo(new LocalDate(time)) == 0;
    }

    public static boolean isTomorrow(DateTime time) {
        return LocalDate.now().plusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    public static boolean isYesterday(DateTime time) {
        return LocalDate.now().minusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    public static int dateCmp(DateTime dateTime1, DateTime dateTime2) {
        return dateTime1.toLocalDate().compareTo(dateTime2.toLocalDate());
    }

    public static DateTime getJodaDateTime(Timestamp timestamp2) {

        try {
            return new DateTime(timestamp2.toDate());
        } catch (Exception e) {
            Log.e(DateTimeUtils.class.toString(), e.toString());
        }

        return null;
    }
}
