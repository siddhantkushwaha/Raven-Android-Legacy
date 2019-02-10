package com.siddhantkushwaha.raven.localEntity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenThread extends RealmObject {

    @PrimaryKey
    private String threadId;

    private String userId;

    // either one of the below two set of properties will be used for thread profiling
    /* -------------------------------------- */
    private RavenUser user;

    private String groupName;
    private String picUrl;
    /* -------------------------------------- */


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

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getPicUrl() {
        return picUrl;
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

    // utility function
    public boolean isGroup() {
        return user == null;
    }
}
