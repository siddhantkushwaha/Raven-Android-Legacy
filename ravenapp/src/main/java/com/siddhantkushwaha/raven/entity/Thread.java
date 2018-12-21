package com.siddhantkushwaha.raven.entity;

import android.support.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Thread {

    private String userId1;
    private String userId2;

    @ServerTimestamp
    private Timestamp timestamp;

    public Thread() {

    }

    public Thread(@NonNull String userId1, @NonNull String userId2) {

        setUserId1(userId1);
        setUserId2(userId2);
    }

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
