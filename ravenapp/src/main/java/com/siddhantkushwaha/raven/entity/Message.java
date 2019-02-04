package com.siddhantkushwaha.raven.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class Message {

    private String text;
    private String fileRef;
    private String sentByUserId;
    private String sentToUserId;

    private Timestamp sentTime;

    @ServerTimestamp
    private Timestamp timestamp;

    private HashMap<String, Timestamp> seenBy;

    private ArrayList<String> deletedBy;

    public Message() {

    }

    public Message(@Nullable String text, @NonNull Timestamp sentTime, @NonNull String sentByUserId, @NonNull String sentToUserId) {

        setText(text);
        setSentTime(sentTime);
        setSentByUserId(sentByUserId);
        setSentToUserId(sentToUserId);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setFileRef(String fileRef) {
        this.fileRef = fileRef;
    }

    public String getFileRef() {
        return fileRef;
    }

    public void setSentByUserId(String sentByUserId) {
        this.sentByUserId = sentByUserId;
    }

    public String getSentByUserId() {
        return sentByUserId;
    }

    public void setSentToUserId(String sentToUserId) {
        this.sentToUserId = sentToUserId;
    }

    public String getSentToUserId() {
        return sentToUserId;
    }

    public void setSentTime(Timestamp sentTime) {
        this.sentTime = sentTime;
    }

    public Timestamp getSentTime() {
        return sentTime;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setSeenBy(HashMap<String, Timestamp> seenBy) {
        this.seenBy = seenBy;
    }

    public HashMap<String, Timestamp> getSeenBy() {
        return seenBy;
    }

    public void setDeletedBy(ArrayList<String> deletedBy) {
        this.deletedBy = deletedBy;
    }

    public ArrayList<String> getDeletedBy() {
        return deletedBy;
    }
}
