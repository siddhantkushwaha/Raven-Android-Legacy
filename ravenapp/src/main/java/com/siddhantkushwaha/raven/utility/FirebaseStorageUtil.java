package com.siddhantkushwaha.raven.utility;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.siddhantkushwaha.android.thugtools.thugtools.utility.LocalStorage;

public class FirebaseStorageUtil {

    private static final String prefix = "gs://raven-f6b32.appspot.com/";

    public interface OnComplete {
        void onComplete(String uri);
    }

    public static void getDownloadUrl(Context context, String fileRef, OnComplete onComplete) {

        String url = LocalStorage.get(context, "fileUrl", fileRef);
        if (url != null) {
            onComplete.onComplete(url);
        } else {
            FirebaseStorage.getInstance().getReference(fileRef.replace(prefix, "")).getDownloadUrl().addOnSuccessListener(uri -> {

                onComplete.onComplete(uri.toString());
                LocalStorage.set(context, "fileUrl", fileRef, uri.toString());
            }).addOnFailureListener(e -> {

                onComplete.onComplete(null);
                LocalStorage.set(context, "fileUrl", fileRef, null);
                e.printStackTrace();
            });
        }
    }

    public static void deleteFile(String fileRef) {
        FirebaseStorage.getInstance().getReference(fileRef.replace(prefix, "")).delete();
    }
}
