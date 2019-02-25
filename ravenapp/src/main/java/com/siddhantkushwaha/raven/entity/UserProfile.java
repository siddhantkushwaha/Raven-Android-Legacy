package com.siddhantkushwaha.raven.entity;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class UserProfile {

    private String name;
    private String about;
    private String picUrl;

    @ServerTimestamp
    private Timestamp timestamp;

    public UserProfile() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPicUrl() {
        return this.picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
