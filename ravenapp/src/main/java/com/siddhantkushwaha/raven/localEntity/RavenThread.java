package com.siddhantkushwaha.raven.localEntity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenThread extends RealmObject {

    @PrimaryKey
    private String threadId;

    private String userId;

    private RavenUser user;
    private RavenMessage lastMessage;

    private Boolean isRead;

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

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Boolean getRead() {
        return isRead;
    }
}
