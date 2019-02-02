package com.siddhantkushwaha.raven.localEntity;

import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.utility.JodaTimeUtilV2;

import org.joda.time.DateTime;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenMessage extends RealmObject {

    @PrimaryKey
    private String messageId;

    private String threadId;

    private String text;
    private String fileRef;
    private String sentByUserId;
    private String sentToUserId;

    // These are in JodaTime
    private String timestamp;
    private String localTimestamp;
    private String seenAt;

    private boolean selected;

    public RavenMessage() {

        setSelected(false);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
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

    public String getSentToUserId() {
        return sentToUserId;
    }

    public void setSentToUserId(String sentToUserId) {
        this.sentToUserId = sentToUserId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocalTimestamp() {
        return localTimestamp;
    }

    public void setLocalTimestamp(String localTimestamp) {
        this.localTimestamp = localTimestamp;
    }

    public String getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(String seenAt) {
        this.seenAt = seenAt;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void cloneObject(Message message) {

        this.setText(message.getText());
        this.setFileRef(message.getFileRef());
        this.setSentByUserId(message.getSentByUserId());
        this.setSentToUserId(message.getSentToUserId());

        DateTime localTime = JodaTimeUtilV2.getJodaDateTimeFromFirebaseTimestamp(message.getSentTime());
        if (localTime != null)
            this.setLocalTimestamp(localTime.toString());

        if (message.getTimestamp() != null) {
            DateTime serverTime = JodaTimeUtilV2.getJodaDateTimeFromFirebaseTimestamp(message.getTimestamp());
            if (serverTime != null)
                this.setTimestamp(serverTime.toString());
        }

        if (message.getSeenAt() != null) {
            DateTime seenTime = JodaTimeUtilV2.getJodaDateTimeFromFirebaseTimestamp(message.getSeenAt());
            if (seenTime != null)
                this.setSeenAt(seenTime.toString());
        }
    }

    public Integer getMessageType(String userId) {
        if (sentByUserId.equals(userId))
            return 1;
        else
            return 2;
    }
}
