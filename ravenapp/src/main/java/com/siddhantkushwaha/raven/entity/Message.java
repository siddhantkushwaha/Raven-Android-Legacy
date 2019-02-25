package com.siddhantkushwaha.raven.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;


public class Message {

    private String text;
    private String fileRef;
    private String sentByUserId;

    private Timestamp sentTime;

    @ServerTimestamp
    private Timestamp timestamp;

    private HashMap<String, Timestamp> seenBy;

    private ArrayList<String> notDeletedBy;

    public Message() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileRef() {
        return fileRef;
    }

    public void setFileRef(String fileRef) {
        this.fileRef = fileRef;
    }

    public String getSentByUserId() {
        return sentByUserId;
    }

    public void setSentByUserId(String sentByUserId) {
        this.sentByUserId = sentByUserId;
    }

    public Timestamp getSentTime() {
        return sentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        this.sentTime = sentTime;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, Timestamp> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(HashMap<String, Timestamp> seenBy) {
        this.seenBy = seenBy;
    }

    public ArrayList<String> getNotDeletedBy() {
        return notDeletedBy;
    }

    public void setNotDeletedBy(ArrayList<String> notDeletedBy) {
        this.notDeletedBy = notDeletedBy;
    }
}
