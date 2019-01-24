package com.siddhantkushwaha.raven.utility;

import android.content.Context;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.siddhantkushwaha.raven.common.utility.RealmUtil;
import com.siddhantkushwaha.raven.localEntity.Pair;

import io.realm.Realm;

public class FirebaseStorageUtil {

    private static final String prefix = "gs://raven-f6b32.appspot.com/";

    public interface OnComplete {
        void onComplete(String uri);
    }

    public void getDownloadUrl(Context context, String fileRef, OnComplete onComplete) {

        Realm realm = RealmUtil.getCustomRealmInstance(context);

        realm.executeTransaction(realmIns1 -> {

            Pair pair = realmIns1.where(Pair.class).equalTo("ref", fileRef).findFirst();
            if (pair != null) {
                onComplete.onComplete(pair.getUri());
            } else {
                FirebaseStorage.getInstance().getReference(fileRef.replace(prefix, "")).getDownloadUrl().addOnSuccessListener(uri -> {

                    onComplete.onComplete(uri.toString());

                    realm.executeTransactionAsync(realmIns2 -> {

                        Pair newPair = new Pair();
                        newPair.setRef(fileRef);
                        newPair.setUri(uri.toString());
                        realmIns2.insertOrUpdate(newPair);
                    });

                }).addOnFailureListener(e -> {
                    Log.i(FirebaseStorageUtil.class.toString(), fileRef);
                    e.printStackTrace();
                });
            }
        });
        realm.close();
    }

    public void deleteFile(String fileRef) {
        FirebaseStorage.getInstance().getReference(fileRef.replace(prefix, "")).delete();
    }
}