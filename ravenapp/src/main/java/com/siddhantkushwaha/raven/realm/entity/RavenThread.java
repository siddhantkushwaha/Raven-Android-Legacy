package com.siddhantkushwaha.raven.realm.entity;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenThread extends RealmObject {

    @PrimaryKey
    public String threadId;
    public String userId;

    public String picUrl;
    public String groupName;
    public RealmList<RavenUser> users;

    public String permissions;

    public RavenMessage lastMessage;

    public String backgroundFileRef;
    public Float backgroundOpacity;

    public String timestamp;

    public boolean isGroup() {
        return groupName != null;
    }

    public RavenUser getUser() {
        if (isGroup())
            throw new RuntimeException("This function should not be called on a group.");

        if (users == null || users.isEmpty())
            return null;

        RavenUser ravenUser = users.get(0);
        if (ravenUser.userId.equals(userId))
            ravenUser = users.get(1);

        return ravenUser;
    }
}
