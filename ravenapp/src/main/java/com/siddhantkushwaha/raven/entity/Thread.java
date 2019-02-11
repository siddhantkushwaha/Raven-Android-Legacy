package com.siddhantkushwaha.raven.entity;

import java.util.ArrayList;

public class Thread {

    private ThreadGroupDetails groupDetails;
    private ArrayList<String> users;
    private ThreadBackground backgroundMetadata;


    public Thread() {

    }

    public ThreadGroupDetails getGroupDetails() {
        return groupDetails;
    }

    public void setGroupDetails(ThreadGroupDetails groupDetails) {
        this.groupDetails = groupDetails;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public ThreadBackground getBackgroundMetadata() {
        return backgroundMetadata;
    }

    public void setBackgroundMetadata(ThreadBackground backgroundMetadata) {
        this.backgroundMetadata = backgroundMetadata;
    }
}
