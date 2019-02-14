package com.siddhantkushwaha.raven.manager;

import android.app.Activity;

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
import com.siddhantkushwaha.raven.custom.AESNygma;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.utility.FirebaseUtils;
import com.siddhantkushwaha.raven.utility.RavenUtils;

import java.security.GeneralSecurityException;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThreadManager {

    private static final String THREAD_COLLECTION_NAME = "threads";
    private static final String MESSAGE_COLLECTION_NAME = "messages";

    private FirebaseFirestore db;

    public ThreadManager() {

        db = FirebaseUtils.getFirestoreDb(true);
    }

    public void sendMessageV2(@NonNull String threadId, @NonNull Message message, String userId) {

        DocumentReference messageRef = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document();

        WriteBatch batch = db.batch();

        if (!userId.equals(RavenUtils.GROUP)) {
            HashMap<String, Object> threadMap = new HashMap<>();
            threadMap.put("users", message.getNotDeletedBy());
            batch.set(db.collection(THREAD_COLLECTION_NAME).document(threadId), threadMap, SetOptions.merge());
        }

        batch.set(messageRef, message);
        batch.commit().addOnCompleteListener(task -> {

//            HashMap<String, String> map = new HashMap<>();
//            map.put("threadId", threadId);
//            map.put("messageId", messageRef.getId());
//            map.put("sentByUserId", FirebaseAuth.getInstance().getUid());
//            FirebaseUtils.getRealtimeDb(true).getReference("messages").push().setValue(map);
        });
    }

//    public void sendMessage(@NonNull String threadId, @NonNull Message message,
//                            @NonNull Uri uri, @Nullable OnProgressListener<UploadTask.TaskSnapshot> onProgressListener) {
//
//        // shortcut to generate a random fileId for now
//        String fileId = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document().getId();
//
//        StorageReference fileRef = FirebaseStorage.getInstance().getReference("thread_media/" + threadId + "/" + fileId + "/media.png");
//        UploadTask uploadTask = fileRef.putFile(uri);
//
//        if (onProgressListener != null)
//            uploadTask.addOnProgressListener(onProgressListener);
//
//        uploadTask.addOnCompleteListener(uTask -> {
//
//            UploadTask.TaskSnapshot taskSnapshot = uTask.getResult();
//            if (uTask.isSuccessful() && taskSnapshot != null) {
//
//                message.setFileRef(taskSnapshot.getStorage().toString());
//                sendMessage(threadId, message);
//            }
//        });
//    }

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
        map.put("fileRef", FieldValue.delete());

        if (fileRef != null)
            new FirebaseStorageUtil().deleteFile(fileRef);

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId).update(map).addOnCompleteListener(onCompleteListener);
    }

    public void deleteForCurrentUser(@NonNull String threadId, @NonNull String messageId) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("notDeletedBy", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId).update(map);
    }

    public void startThreadSyncByThreadId(Activity activity, String threadId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).whereArrayContains("notDeletedBy", FirebaseAuth.getInstance().getUid()).addSnapshotListener(activity, eventListener);
    }

    public void startThreadDocSyncByThreadId(Activity activity, String threadId, EventListener<DocumentSnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).addSnapshotListener(activity, eventListener);
    }

    public void syncAllThreadsByUserId(Activity activity, String userId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).whereArrayContains("users", userId).addSnapshotListener(eventListener);
    }

    public void startLastMessageSyncByTimestamp(Activity activity, String threadId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME)
                .whereArrayContains("notDeletedBy", FirebaseAuth.getInstance().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(eventListener);
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
