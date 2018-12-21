package com.siddhantkushwaha.raven.entity;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ThreadIndex {

    private String threadId;
    private String lastMessageId;

    @ServerTimestamp
    private Timestamp timestamp;

    public ThreadIndex() {

    }

    public ThreadIndex(String threadId, String lastMessageId) {
        setThreadId(threadId);
        setLastMessageId(lastMessageId);
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
