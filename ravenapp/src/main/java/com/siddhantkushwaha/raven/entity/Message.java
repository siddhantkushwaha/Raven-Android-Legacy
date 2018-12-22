package com.siddhantkushwaha.raven.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;


public class Message {

    private String text;
    private String fileRef;
    private String sentByUserId;
    private String sentToUserId;

    private Timestamp sentTime;

    @ServerTimestamp
    private Timestamp timestamp;

    private Timestamp seenAt;

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

    public void setSeenAt(Timestamp seenAt) {
        this.seenAt = seenAt;
    }

    public Timestamp getSeenAt() {
        return seenAt;
    }
}
