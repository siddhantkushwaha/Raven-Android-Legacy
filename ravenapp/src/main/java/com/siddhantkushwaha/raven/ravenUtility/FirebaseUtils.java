package com.siddhantkushwaha.raven.ravenUtility;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseUtils {

    public static FirebaseFirestore getFirestoreDb(Boolean persistenceEnabled) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .setPersistenceEnabled(persistenceEnabled).build();
        try {
            db.setFirestoreSettings(settings);
        } catch (Exception e) {
            Log.e("FIREBASE_UTILS_RAVEN", e.toString());
        }
        return db;
    }

    public static FirebaseDatabase getRealtimeDb() {

        return FirebaseDatabase.getInstance();
    }
}
