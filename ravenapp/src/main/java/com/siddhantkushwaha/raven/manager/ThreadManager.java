package com.siddhantkushwaha.raven.manager;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.siddhantkushwaha.raven.custom.AESNygma;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.entity.ThreadIndex;
import com.siddhantkushwaha.raven.ravenUtility.FirebaseUtils;

import java.security.GeneralSecurityException;
import java.util.HashMap;

public class ThreadManager {

    private static final String KEY_USER_ID_1 = "userId1";
    private static final String KEY_USER_ID_2 = "userId2";

    private static final String THREAD_COLLECTION_NAME = "threads";
    private static final String MESSAGE_COLLECTION_NAME = "messages";

    private static final String THREAD_INDEX_COLLECTION_NAME = "threadIndexes";
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
        ThreadIndex threadIndex = new ThreadIndex(threadId, messageRef.getId());

        batch.set(db.collection(THREAD_COLLECTION_NAME).document(threadId), threadMap);
        batch.set(messageRef, message);

        batch.set(db.collection(USER_INDEX_COLLECTION_NAME).document(message.getSentByUserId()).collection(THREAD_INDEX_COLLECTION_NAME).document(message.getSentToUserId()), threadIndex);
        batch.set(db.collection(USER_INDEX_COLLECTION_NAME).document(message.getSentToUserId()).collection(THREAD_INDEX_COLLECTION_NAME).document(message.getSentByUserId()), threadIndex);

        batch.commit().addOnCompleteListener(task -> {

            HashMap<String, String> map = new HashMap<>();
            map.put("threadId", threadId);
            map.put("messageId", messageRef.getId());
            FirebaseUtils.getRealtimeDb().getReference("messages").push().setValue(map);
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
        map.put("seenAt", timestamp);

        DocumentReference messageRef = db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId);
        db.runTransaction(transaction -> {
            DocumentSnapshot message = transaction.get(messageRef);
            if (!FirebaseAuth.getInstance().getUid().equals(message.get("sentByUserId")) && message.get("seenAt") == null) {
                transaction.update(messageRef, map);
            }
            return null;
        }).addOnCompleteListener(onCompleteListener);
    }

    public void deleteMessageForEveryone(@NonNull String threadId, @NonNull String messageId, OnCompleteListener<Void> onCompleteListener) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("text", FieldValue.delete());
        map.put("fileRef", FieldValue.delete());

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId).update(map).addOnCompleteListener(onCompleteListener);
    }

    public void startThreadSyncByThreadId(Activity activity, String threadId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).addSnapshotListener(activity, eventListener);
    }

    public void startSyncThreadIndexByUserId(Activity activity, String userId, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(USER_INDEX_COLLECTION_NAME).document(userId).collection(THREAD_INDEX_COLLECTION_NAME).addSnapshotListener(activity, eventListener);
    }

    public void startMessageSyncByMessageId(Activity activity, String threadId, String messageId, EventListener<DocumentSnapshot> eventListener) {

        if (activity == null)
            return;

        db.collection(THREAD_COLLECTION_NAME).document(threadId).collection(MESSAGE_COLLECTION_NAME).document(messageId).addSnapshotListener(eventListener);
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

    public static Message decryptMessage(String threadId, Message message) {

        if (message == null)
            return null;

        try {
            String decryptedMessage = decryptMessage(threadId, message.getText());
            message.setText(decryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            message.setText("There was a problem.");
        }
        return message;
    }
}
