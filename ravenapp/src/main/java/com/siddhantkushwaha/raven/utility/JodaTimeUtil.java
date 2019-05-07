package com.siddhantkushwaha.raven.utility;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Date;


public class JodaTimeUtil {

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

    public static DateTime getJodaDateTime(Date date) {
        return new DateTime(date);
    }

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
