package com.siddhantkushwaha.raven.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class Thread {

    private ArrayList<String> users;
    private HashMap<String, Message> messages;

    private ThreadGroupDetails groupDetails;
    private ThreadBackground backgroundMetadata;

    public Thread() {

    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public HashMap<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, Message> messages) {
        this.messages = messages;
    }

    public ThreadGroupDetails getGroupDetails() {
        return groupDetails;
    }

    public void setGroupDetails(ThreadGroupDetails groupDetails) {
        this.groupDetails = groupDetails;
    }

    public ThreadBackground getBackgroundMetadata() {
        return backgroundMetadata;
    }

    public void setBackgroundMetadata(ThreadBackground backgroundMetadata) {
        this.backgroundMetadata = backgroundMetadata;
    }
}
