package com.siddhantkushwaha.raven.manager;

import android.app.Activity;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.siddhantkushwaha.raven.utility.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class UserManager {

    private static String TAG = "USER_MANAGER";

    public static final String KEY_PHONE = "phoneNumber";
    public static final String PRETTY_PHONE = "Phone Number";

    public static final String KEY_USER_ID = "userId";

    public static final String KEY_NAME = "userProfile.name";
    public static final String PRETTY_NAME = "Name";

    public static final String KEY_ABOUT = "userProfile.about";
    public static final String PRETTY_ABOUT = "About";

    public static final String KEY_PROFILE_PIC = "userProfile.picUrl";
    public static final String PRETTY_PROFILE_PIC = "Profile Picture";

    public static final String KEY_LOCATION = "userLocation";
    public static final String PRETTY_LOCATION = "Location";
    public static final String KEY_LOCATION_PRIVACY = "userLocation.privacyStatus";
    public static final String PRETTY_LOCATION_PRIVACY = "Privacy of Location";
    public static final String KEY_LOCATION_TIME = "userLocation.time";
    public static final String KEY_LOCATION_TIMESTAMP = "userLocation.timestamp";
    public static final String KEY_LOCATION_LATITUDE = "userLocation.latitude";
    public static final String KEY_LOCATION_LONGITUDE = "userLocation.longitude";

    public static final String KEY_USER_PRESENCE_ONLINE = "userPresence.online";
    public static final String KEY_USER_PRESENCE_LAST_SEEN = "userPresence.lastSeen";

    public static final String ENUM_USER_PRIVACY_PUBLIC = "PUBLIC";
    public static final String ENUM_USER_PRIVACY_CONTACTS = "CONTACTS";
    public static final String ENUM_USER_PRIVACY_NONE = "NONE";

    private static final String COLLECTION_NAME = "users";
    private FirebaseFirestore db;

    /* start ==> default functions */

    public UserManager() {

        db = FirebaseUtils.getFirestoreDb(true);
    }

    /* end ==> default functions */

    public static String getPrettyName(String fieldKey) {

        String prettyName = "";
        switch (fieldKey) {
            case KEY_PHONE:
                prettyName = PRETTY_PHONE;
                break;
            case KEY_NAME:
                prettyName = PRETTY_NAME;
                break;
            case KEY_ABOUT:
                prettyName = PRETTY_ABOUT;
                break;
            case KEY_PROFILE_PIC:
                prettyName = PRETTY_PROFILE_PIC;
                break;
            case KEY_LOCATION:
                prettyName = PRETTY_LOCATION;
                break;
            case KEY_LOCATION_PRIVACY:
                prettyName = PRETTY_LOCATION_PRIVACY;
                break;
        }
        return prettyName;
    }

    public void setUserFields(String userId, Map<String, Object> map, OnCompleteListener<Void> onCompleteListener) {

        DocumentReference documentReference = db.collection(COLLECTION_NAME).document(userId);
        documentReference.set(map, SetOptions.merge()).addOnCompleteListener(onCompleteListener);
    }

    public void updateUserFields(String userId, Map<String, Object> map, OnCompleteListener<Void> onCompleteListener) {

        DocumentReference documentReference = db.collection(COLLECTION_NAME).document(userId);
        documentReference.update(map).addOnCompleteListener(onCompleteListener);
    }

    public void setUserMetaData(String userId, HashMap<String, Object> map, OnCompleteListener<Void> onCompleteListener) {
        FirebaseUtils.getRealtimeDb(true).getReference("user_metadata/" + userId).setValue(map).addOnCompleteListener(onCompleteListener);
    }

    public void startUserSyncByUserId(Activity activity, String userId, EventListener<DocumentSnapshot> eventListener) {

        if (activity == null)
            return;

        DocumentReference documentReference = db.collection(COLLECTION_NAME).document(userId);
        documentReference.addSnapshotListener(activity, eventListener);
    }

    public void getUserByUserId(@NonNull String userId, OnCompleteListener<DocumentSnapshot> onCompleteListener) {

        db.collection(COLLECTION_NAME).document(userId).get().addOnCompleteListener(onCompleteListener);
    }

    public void startUserSyncByAttribute(Activity activity, String attributeKey, Object attributeValue, EventListener<QuerySnapshot> eventListener) {

        if (activity == null)
            return;

        Query query = db.collection(COLLECTION_NAME).whereEqualTo(attributeKey, attributeValue);
        query.addSnapshotListener(activity, eventListener);
    }

    public void startGetUserByAttribute(String attributeKey, Object attributeValue, OnCompleteListener<QuerySnapshot> eventListener) {

        FirebaseUtils.getFirestoreDb(true).collection(COLLECTION_NAME).whereEqualTo(attributeKey, attributeValue).get().addOnCompleteListener(eventListener);
    }

    public void updateProfilePicture(Uri uri, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener) {

        StorageReference ref = FirebaseStorage.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid() + "/" + "profile_media/display_pic.png");
        UploadTask uploadTask = ref.putFile(uri);
        uploadTask.addOnCompleteListener(onCompleteListener);

        // TODO remove this later
        FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getUid() + "/" + "profile_media/display_pic.png").delete();

//        new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                if(task.isSuccessful()) {
//
//                    UploadTask.TaskSnapshot taskSnapshot = task.getResult();
//                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener()
//                }
//            }
//        }
    }
}
