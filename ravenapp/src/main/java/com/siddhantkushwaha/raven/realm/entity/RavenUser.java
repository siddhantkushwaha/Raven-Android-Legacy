package com.siddhantkushwaha.raven.realm.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenUser extends RealmObject {

    @PrimaryKey
    public String userId;

    public String phoneNumber;
    public String displayName;
    public String about;
    public String picUrl;

    public String contactName;
}
