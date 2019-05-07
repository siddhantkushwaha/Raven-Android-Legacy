package com.siddhantkushwaha.raven.utility;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

public class RavenUtils {

    public static final String INVALID = "INVALID";
    public static final String GROUP = "THIS_IS_A_GROUP";

    public static String getThreadId(String userId1, String userId2) {

        if (userId1 == null || userId2 == null)
            return null;

        if (userId1.equals(userId2))
            return INVALID;

        ArrayList<String> userIds = new ArrayList<>();
        userIds.add(userId1);
        userIds.add(userId2);
        Collections.sort(userIds, String::compareToIgnoreCase);
        return userIds.get(0) + userIds.get(1);
    }

    public static String getUserId(@NonNull String threadId, @NonNull String userId) {

        if (threadId.contains(userId))
            return threadId.replace(userId, "");
        else
            return GROUP;
    }

    public static Boolean isGroup(@NonNull String threadId, @NonNull String userId) {

        return !threadId.contains(userId);
    }
}
