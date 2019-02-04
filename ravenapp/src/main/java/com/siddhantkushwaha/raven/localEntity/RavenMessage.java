package com.siddhantkushwaha.raven.localEntity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.siddhantkushwaha.nuttertools.GsonUtil;
import com.siddhantkushwaha.raven.entity.Message;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;
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

    private String seenBy;

    private String deletedBy;

    private RealmList<String> deletedByList;

    private boolean selected = false;

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

    public void setSeenBy(String seenBy) {
        this.seenBy = seenBy;
    }

    public String getSeenBy() {
        return seenBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedByList(RealmList<String> deletedByList) {
        this.deletedByList = deletedByList;
    }

    public RealmList<String> getDeletedByList() {
        return deletedByList;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void cloneObject(Message message) {

        setText(message.getText());
        setFileRef(message.getFileRef());
        setSentByUserId(message.getSentByUserId());
        setSentToUserId(message.getSentToUserId());

        setLocalTimestamp(new DateTime(message.getSentTime().toDate()).toString());

        if (message.getTimestamp() != null)
            setTimestamp(new DateTime(message.getTimestamp().toDate()).toString());

        if (message.getDeletedBy() != null) {
            RealmList<String> arr = new RealmList<>();
            arr.addAll(message.getDeletedBy());
            setDeletedByList(arr);

            if (message.getDeletedBy().contains(FirebaseAuth.getInstance().getUid()))
                setDeletedBy(FirebaseAuth.getInstance().getUid());
        }

        if (message.getSeenAt() != null)
            setSeenAt(new DateTime(message.getSeenAt().toDate()).toString());

        if (message.getSeenBy() != null) {
            HashMap<String, String> temp = new HashMap<>();
            for (Map.Entry<String, Timestamp> entry : message.getSeenBy().entrySet()) {
                temp.put(entry.getKey(), new DateTime(entry.getValue().toDate()).toString());
            }
            setSeenBy(GsonUtil.toGson(temp));
        }
    }

    public Integer getMessageType(String userId) {
        if (sentByUserId.equals(userId))
            return 1;
        else
            return 2;
    }
}
