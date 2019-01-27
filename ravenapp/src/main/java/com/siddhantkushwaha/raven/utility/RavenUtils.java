package com.siddhantkushwaha.raven.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;

public class RavenUtils {

    public static String getThreadId(String userId1, String userId2) {

        if(userId1 == null || userId2 == null)
            return null;

        if(userId1.equals(userId2))
            return null;

        ArrayList<String> userIds = new ArrayList<>();
        userIds.add(userId1);
        userIds.add(userId2);
        Collections.sort(userIds, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        return userIds.get(0) + userIds.get(1);
    }

    public static String getUserId(@NonNull String threadId, @NonNull String userId) {

        return threadId.replace(userId, "");
    }
}
