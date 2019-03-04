package com.siddhantkushwaha.raven.manager;

import android.app.Activity;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.siddhantkushwaha.raven.custom.AESNygma;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.entity.Thread;
import com.siddhantkushwaha.raven.entity.ThreadGroupDetails;
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.utility.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThreadManager {

    private static final String THREAD_COLLECTION_NAME = "threads";
    private static final String KEY_MESSAGES = "messages";

    private FirebaseFirestore db;

    public ThreadManager() {

        db = FirebaseUtils.getFirestoreDb(true);
    }

    public static String encryptMessage(String threadId, String message) {

        String encryptedMessage = null;
        try {
            encryptedMessage = AESNygma.encrypt(threadId, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    public static String decryptMessage(String threadId, String encryptedMessage) throws Exception {

        return AESNygma.decrypt(threadId, encryptedMessage);
    }

    public void createGroup(@NonNull String name, @NonNull ArrayList<String> users, @NonNull HashMap<String, String> permissions, OnCompleteListener<DocumentReference> onCompleteListener) {

        if (users.isEmpty())
            return;

        Thread thread = new Thread();
        ThreadGroupDetails threadGroupDetails = new ThreadGroupDetails();

        threadGroupDetails.setName(name);
        threadGroupDetails.setPermissions(permissions);

        thread.setGroupDetails(threadGroupDetails);

        thread.setUsers(users);

        db.collection(THREAD_COLLECTION_NAME).add(thread).addOnCompleteListener(onCompleteListener);
    }

    public void sendMessage(@NonNull String threadId, @NonNull String messageId, @NonNull Message message, boolean isGroup) {

        WriteBatch batch = db.batch();

        HashMap<String, Object> threadMap = new HashMap<>();

        if (!isGroup)
            threadMap.put("users", FieldValue.arrayUnion(message.getNotDeletedBy().toArray()));

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put(messageId, message);
        threadMap.put(KEY_MESSAGES, messageMap);

        batch.set(db.collection(THREAD_COLLECTION_NAME).document(threadId), threadMap, SetOptions.merge());

        batch.commit().addOnCompleteListener(task -> {

            HashMap<String, Object> map = new HashMap<>();
            map.put("threadId", threadId);
            map.put("messageId", messageId);
            message.getNotDeletedBy().remove(FirebaseAuth.getInstance().getUid());
            map.put("sentTo", message.getNotDeletedBy());
            FirebaseUtils.getRealtimeDb(true).getReference(KEY_MESSAGES).push().setValue(map);
        });
    }

    public void markMessageAsRead(@NonNull String threadId, @NonNull String messageId, Timestamp timestamp, OnCompleteListener<Object> onCompleteListener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("messages." + messageId + ".seenBy." + FirebaseAuth.getInstance().getUid(), timestamp);

        DocumentReference threadRef = db.collection(THREAD_COLLECTION_NAME).document(threadId);
        db.runTransaction(transaction -> {

            DocumentSnapshot threadSnap = transaction.get(threadRef);
            try {
                Thread thread = threadSnap.toObject(Thread.class);
                Message message = thread.getMessages().get(messageId);
                if (!message.getSentByUserId().equals(FirebaseAuth.getInstance().getUid())) {
                    if (message.getSeenBy() == null)
                        transaction.update(threadRef, map);
                    else if (!message.getSeenBy().containsKey(FirebaseAuth.getInstance().getUid()))
                        transaction.update(threadRef, map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }).addOnCompleteListener(onCompleteListener);
    }

    public void deleteMessageForEveryone(@NonNull String threadId, @NonNull String messageId, @Nullable String fileRef, OnCompleteListener<Void> onCompleteListener) {

        if (fileRef != null)
            FirebaseStorageUtil.deleteFile(fileRef);

        HashMap<String, Object> map = new HashMap<>();
        map.put("messages." + messageId + ".text", FieldValue.delete());
        map.put("messages." + messageId + ".fileRef", FieldValue.delete());

        db.collection(THREAD_COLLECTION_NAME).document(threadId).update(map).addOnCompleteListener(onCompleteListener);
    }

    public void deleteForCurrentUser(@NonNull String threadId, @NonNull String messageId) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("messages." + messageId + ".notDeletedBy", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

        db.collection(THREAD_COLLECTION_NAME).document(threadId).update(map);
    }

    public void startThreadSyncByThreadId(Activity activity, String threadId, EventListener<DocumentSnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).addSnapshotListener(activity, eventListener);
    }

    public void startAllThreadsSyncByUserId(Activity activity, String userId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).whereArrayContains("users", userId).addSnapshotListener(activity, eventListener);
    }

    public void changeThreadBackground(@NonNull String fileRef, @NonNull Float opacity, @NonNull String threadId, @NonNull String userId, OnCompleteListener<Void> onCompleteListener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("backgroundMetadata.fileRef", fileRef);
        map.put("backgroundMetadata.opacity", opacity);
        map.put("backgroundMetadata.changedByUserId", userId);
        db.collection(THREAD_COLLECTION_NAME).document(threadId).update(map).addOnCompleteListener(onCompleteListener);
    }

    public void deleteThreadBackground(@NonNull String threadId, @NonNull String userId, OnCompleteListener<Void> onCompleteListener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("backgroundMetadata", FieldValue.delete());
        db.collection(THREAD_COLLECTION_NAME).document(threadId).update(map).addOnCompleteListener(onCompleteListener);
    }
}