package com.siddhantkushwaha.raven.localEntity;

import android.os.Bundle;

import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenThread extends RealmObject {

    @PrimaryKey
    private String threadId;

    private String userId;

    private RavenUser user;
    private RavenMessage lastMessage;

    private String backgroundFileUrl;
    private Float backgroundOpacity;

    private String timestamp;

    public RavenThread() {

    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUser(RavenUser user) {
        this.user = user;
    }

    public RavenUser getUser() {
        return user;
    }

    public void setLastMessage(RavenMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public RavenMessage getLastMessage() {
        return lastMessage;
    }

    public String getBackgroundFileUrl() {
        return backgroundFileUrl;
    }

    public void setBackgroundFileUrl(String backgroundFileUrl) {
        this.backgroundFileUrl = backgroundFileUrl;
    }

    public Float getBackgroundOpacity() {
        return backgroundOpacity;
    }

    public void setBackgroundOpacity(Float backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
