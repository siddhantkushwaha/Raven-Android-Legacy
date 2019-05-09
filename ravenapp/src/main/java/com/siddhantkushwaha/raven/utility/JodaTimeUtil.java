package com.siddhantkushwaha.raven.utility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;


public class JodaTimeUtil {

    public static boolean isToday(@NonNull DateTime time) {
        time = getInDefaultTimeZone(time);
        return LocalDate.now(DateTimeZone.getDefault()).compareTo(new LocalDate(time)) == 0;
    }

    public static boolean isTomorrow(@NonNull DateTime time) {
        time = getInDefaultTimeZone(time);
        return LocalDate.now(DateTimeZone.getDefault()).plusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    public static boolean isYesterday(@NonNull DateTime time) {
        time = getInDefaultTimeZone(time);
        return LocalDate.now(DateTimeZone.getDefault()).minusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    public static boolean sameDate(@NonNull DateTime dateTime1, @NonNull DateTime dateTime2) {

        LocalDate localDate1 = getInDefaultTimeZone(dateTime1).toLocalDate();
        LocalDate localDate2 = getInDefaultTimeZone(dateTime2).toLocalDate();

        return localDate1.compareTo(localDate2) == 0;
    }

    public static DateTime getInDefaultTimeZone(@NonNull DateTime dateTime) {
        return dateTime.toDateTime(DateTimeZone.getDefault());
    }

    public static DateTime getDateTime(@Nullable Timestamp timestamp) {

        if (timestamp == null)
            return null;

        DateTime dateTime = new DateTime(timestamp.toDate());
        return getInDefaultTimeZone(dateTime);
    }

    public static String getDateTimeAsString(@Nullable Timestamp timestamp) {

        DateTime dateTime = getDateTime(timestamp);

        if (dateTime == null)
            return null;

        return dateTime.toString();
    }


}
