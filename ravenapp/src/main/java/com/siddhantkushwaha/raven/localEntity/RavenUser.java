package com.siddhantkushwaha.raven.localEntity;

import com.siddhantkushwaha.raven.entity.User;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenUser extends RealmObject {

    @PrimaryKey
    private String userId;

    private String contactName;
    private String phoneNumber;
    private String displayName;
    private String about;
    private String picUrl;

    public RavenUser() {

    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getAbout() {
        return about;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void cloneObject(User user) {

        setPhoneNumber(user.getPhoneNumber());

        if (user.getUserProfile() != null) {
            setDisplayName(user.getUserProfile().getName());
            setAbout(user.getUserProfile().getAbout());
            setPicUrl(user.getUserProfile().getPicUrl());
        }
    }
}
