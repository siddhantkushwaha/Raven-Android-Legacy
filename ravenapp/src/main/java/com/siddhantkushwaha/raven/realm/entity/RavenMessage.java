package com.siddhantkushwaha.raven.realm.entity;

import com.siddhantkushwaha.nuttertools.GsonUtil;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RavenMessage extends RealmObject {

    @PrimaryKey
    public String messageId;

    public String threadId;

    @Nullable
    public String text;
    @Nullable
    public String fileRef;
    @Nullable
    public String sentByUserId;

    // These are in JodaTime
    @Nullable
    public String timestamp;
    @Nullable
    public String localTimestamp;

    @Nullable
    public String seenBy;

    @Nullable
    public String deletedBy;

    @Nullable
    public RealmList<String> notDeletedBy;

    public boolean selected = false;

    public Integer getMessageType(@NonNull String userId) {

        if (sentByUserId == null)
            return -1;

        if (sentByUserId.equals(userId))
            if (fileRef == null)
                return 1;
            else
                return 2;
        else if (fileRef == null)
            return 3;
        else
            return 4;
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
        if (notDeletedBy != null && notDeletedBy.size() > 0) {
            for (String userId : notDeletedBy) {
                if (getSeenByUserId(userId) == null && !userId.equals(sentByUserId)) {
                    seenByAll = false;
                    break;
                }
            }
        } else
            seenByAll = false;

        return seenByAll;
    }
}
