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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.siddhantkushwaha.raven.custom.AESNygma;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.utility.FirebaseUtils;

import java.security.GeneralSecurityException;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThreadManager {

    private static final String KEY_USER_ID_1 = "userId1";
    private static final String KEY_USER_ID_2 = "userId2";

    private static final String THREAD_COLLECTION_NAME = "threads";
    private static final String MESSAGE_COLLECTION_NAME = "messages";

    private static final String USER_INDEX_COLLECTION_NAME = "userIndexes";

    private FirebaseFirestore db;

    public ThreadManager() {

        db = FirebaseUtils.getFirestoreDb(true);
    }

    public void sendMessage(@NonNull String threadId, @NonNull Message message) {

        HashMap<String, Object> threadMap = new HashMap<>();
        threadMap.put(ThreadManager.KEY_USER_ID_1, message.getSentByUserId());
        threadMap.put(ThreadManager.KEY_USER_ID_2, message.getSentToUserId());

        WriteBatch batch = db.batch();

        DocumentReference messageRef = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document();

        batch.set(db.collection(THREAD_COLLECTION_NAME).document(threadId), threadMap, SetOptions.merge());
        batch.set(messageRef, message);

        HashMap<String, String> map1 = new HashMap<>();
        HashMap<String, HashMap<String, String>> map2 = new HashMap<>();
        map1.put(threadId, "UN_ARCHIVED");
        map2.put("threadIndexes", map1);

        batch.set(db.collection(USER_INDEX_COLLECTION_NAME).document(message.getSentByUserId()), map2, SetOptions.merge());
        batch.set(db.collection(USER_INDEX_COLLECTION_NAME).document(message.getSentToUserId()), map2, SetOptions.merge());

        batch.commit().addOnCompleteListener(task -> {

            HashMap<String, String> map = new HashMap<>();
            map.put("threadId", threadId);
            map.put("messageId", messageRef.getId());
            map.put("sentByUserId", FirebaseAuth.getInstance().getUid());
            FirebaseUtils.getRealtimeDb(true).getReference("messages").push().setValue(map);
        });
    }

    public void sendMessage(@NonNull String threadId, @NonNull Message message,
                            @NonNull Uri uri, @Nullable OnProgressListener<UploadTask.TaskSnapshot> onProgressListener) {

        // shortcut to generate a random fileId for now
        String fileId = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document().getId();

        StorageReference fileRef = FirebaseStorage.getInstance().getReference("thread_media/" + threadId + "/" + fileId + "/media.png");
        UploadTask uploadTask = fileRef.putFile(uri);

        if (onProgressListener != null)
            uploadTask.addOnProgressListener(onProgressListener);

        uploadTask.addOnCompleteListener(uTask -> {

            UploadTask.TaskSnapshot taskSnapshot = uTask.getResult();
            if (uTask.isSuccessful() && taskSnapshot != null) {

                message.setFileRef(taskSnapshot.getStorage().toString());
                sendMessage(threadId, message);
            }
        });
    }

    public void markMessageAsRead(@NonNull String threadId, @NonNull String messageId, Timestamp timestamp, OnCompleteListener<Object> onCompleteListener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("seenBy." + FirebaseAuth.getInstance().getUid(), timestamp);

        DocumentReference messageRef = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId);
        db.runTransaction(transaction -> {

            DocumentSnapshot messageSnap = transaction.get(messageRef);
            try {
                Message message = messageSnap.toObject(Message.class);
                if (!message.getSentByUserId().equals(FirebaseAuth.getInstance().getUid())) {
                    if (message.getSeenBy() == null)
                        transaction.update(messageRef, map);
                    else if (!message.getSeenBy().containsKey(FirebaseAuth.getInstance().getUid()))
                        transaction.update(messageRef, map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }).addOnCompleteListener(onCompleteListener);
    }

    public void deleteMessageForEveryone(@NonNull String threadId, @NonNull String messageId, @Nullable String fileRef, OnCompleteListener<Void> onCompleteListener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("text", FieldValue.delete());

        if (fileRef != null) {
            map.put("fileRef", FieldValue.delete());
            new FirebaseStorageUtil().deleteFile(fileRef);
        }

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId).update(map).addOnCompleteListener(onCompleteListener);
    }

    public void deleteForCurrentUser(@NonNull String threadId, @NonNull String messageId, OnCompleteListener<Object> onCompleteListener) {


        DocumentReference messageRef = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId);
        db.runTransaction(transaction -> {

            DocumentSnapshot messageSnap = transaction.get(messageRef);
            try {
                Message message = messageSnap.toObject(Message.class);
                // means the message the message belongs to a chat between two users
                if (message.getSentToUserId() != null) {
                    if (message.getDeletedBy() == null) {

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("deletedBy", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                        transaction.update(messageRef, map);
                    } else if (!message.getDeletedBy().contains(FirebaseAuth.getInstance().getUid())) {

                        if (message.getFileRef() != null)
                            new FirebaseStorageUtil().deleteFile(message.getFileRef());

                        transaction.delete(messageRef);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }).addOnCompleteListener(onCompleteListener);
    }

    public void startThreadSyncByThreadId(Activity activity, String threadId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).addSnapshotListener(activity, eventListener);
    }

    public void startThreadDocSyncByThreadId(Activity activity, String threadId, EventListener<DocumentSnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).addSnapshotListener(activity, eventListener);
    }

    public void startSyncThreadIndexByUserId(Activity activity, String userId, EventListener<DocumentSnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(USER_INDEX_COLLECTION_NAME).document(userId).addSnapshotListener(activity, eventListener);
    }

    public void startLastMessageSyncByTimestamp(Activity activity, String threadId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).addSnapshotListener(eventListener);
    }

    public void getMessageByMessageId(@NonNull String threadId, @NonNull String messageId, OnCompleteListener<DocumentSnapshot> onCompleteListener) {

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId).get().addOnCompleteListener(onCompleteListener);
    }

    public static String encryptMessage(String threadId, String message) {

        String encryptedMessage = null;
        try {
            encryptedMessage = AESNygma.encrypt(threadId, message);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    public static String decryptMessage(String threadId, String encryptedMessage) throws Exception {

        return AESNygma.decrypt(threadId, encryptedMessage);
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
