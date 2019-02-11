package com.siddhantkushwaha.raven.localEntity;

import com.siddhantkushwaha.raven.entity.Thread;

import androidx.annotation.NonNull;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenThread extends RealmObject {

    @PrimaryKey
    private String threadId;

    private String userId;

    // either one of the below two set of properties will be used for thread profiling
    /* -------------------------------------- */
    private RavenUser user;
    private RealmList<RavenUser> users;

    private String groupName;
    private String picUrl;
    /* -------------------------------------- */


    private RavenMessage lastMessage;

    private String backgroundFileRef;
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

    public String getBackgroundFileRef() {
        return backgroundFileRef;
    }

    public void setBackgroundFileRef(String backgroundFileRef) {
        this.backgroundFileRef = backgroundFileRef;
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
    public void cloneObject(@NonNull Thread thread) {

        if (thread.getGroupDetails() != null) {
            groupName = thread.getGroupDetails().getName();
            picUrl = thread.getGroupDetails().getPicUrl();
        } else {
            groupName = null;
            picUrl = null;
        }

        if (thread.getBackgroundMetadata() != null) {
            backgroundFileRef = thread.getBackgroundMetadata().getFileRef();
            backgroundOpacity = thread.getBackgroundMetadata().getOpacity();
        } else {
            backgroundFileRef = null;
            backgroundOpacity = null;
        }
    }

    public boolean isGroup() {
        return user == null;
    }


}
