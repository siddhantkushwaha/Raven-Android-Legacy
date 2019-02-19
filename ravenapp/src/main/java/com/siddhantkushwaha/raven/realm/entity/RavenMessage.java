package com.siddhantkushwaha.raven.realm.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.siddhantkushwaha.nuttertools.GsonUtil;
import com.siddhantkushwaha.raven.entity.Message;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenMessage extends RealmObject {

    @PrimaryKey
    public String messageId;

    public String threadId;

    public String text;
    public String fileRef;
    public String sentByUserId;
    public RealmList<String> sentTo;

    // These are in JodaTime
    public String timestamp;
    public String localTimestamp;

    public String seenBy;

    public String deletedBy;

    public RealmList<String> notDeletedBy;

    public boolean selected = false;

    public Integer getMessageType(String userId) {
        if (sentByUserId.equals(userId))
            return 1;
        else
            return 2;
    }

    public String getSeenByUserId(@NonNull String userId) {
        String value = null;
        try {
            HashMap<String, String> map = GsonUtil.fromGson(seenBy, HashMap.class);
            value = map.get(userId);
        } catch (Exception e) {
            //pass
        }
        return value;
    }

    public boolean isSeenByAll() {

        boolean seenByAll = true;
        if (sentTo != null && sentTo.size() > 0) {
            for (String userId : sentTo) {
                if (getSeenByUserId(userId) == null) {
                    seenByAll = false;
                    break;
                }
            }
        } else
            seenByAll = false;

        return seenByAll;
    }
}
