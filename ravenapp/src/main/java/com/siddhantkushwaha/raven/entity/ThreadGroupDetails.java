package com.siddhantkushwaha.raven.entity;

import java.util.HashMap;

public class ThreadGroupDetails {

    private String name;
    private String picUrl;

    private HashMap<String, String> permissions;

    public ThreadGroupDetails() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public HashMap<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(HashMap<String, String> permissions) {
        this.permissions = permissions;
    }
}
