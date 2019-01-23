package com.siddhantkushwaha.raven.syncAdapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.siddhantkushwaha.raven.common.utility.ContactsUtil;
import com.siddhantkushwaha.raven.common.utility.RealmUtil;
import com.siddhantkushwaha.raven.entity.User;
import com.siddhantkushwaha.raven.localEntity.RavenUser;
import com.siddhantkushwaha.raven.manager.UserManager;
import com.siddhantkushwaha.raven.utility.CurrentFirebaseUser;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmResults;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SYNC_ADAPTER";
    private Context mContext;

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;

        if (FirebaseApp.getApps(mContext).isEmpty()) {
            FirebaseApp.initializeApp(mContext, FirebaseOptions.fromResource(mContext));
        }

        Fabric.with(context, new Crashlytics());
        Crashlytics.setUserIdentifier(CurrentFirebaseUser.getPhoneNumber());
        Crashlytics.setUserName(CurrentFirebaseUser.getUid());
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        syncContacts(mContext);
    }

    public static void syncContacts(Context context) {

        Log.i(TAG, "PERFORMING_SYNC");

        HashMap<String, String> contactsList = ContactsUtil.getAllContacts(context);

        Realm realm = RealmUtil.getCustomRealmInstance(context);

        realm.executeTransactionAsync(realmIns -> {
            RealmResults<RavenUser> realmResults = realmIns.where(RavenUser.class).findAll();
            for (RavenUser contact : realmResults) {
                if (!contactsList.containsKey(contact.getPhoneNumber())) {
                    realmIns.where(RavenUser.class).equalTo("phoneNumber", contact.getPhoneNumber()).findAll().deleteAllFromRealm();
                }
            }
        });
        realm.close();

        UserManager userManager = new UserManager();
        for (Map.Entry<String, String> contact : contactsList.entrySet()) {
            System.out.println(contact.getKey() + " " + contact.getValue());

            userManager.startGetUserByAttribute(UserManager.KEY_PHONE, contact.getKey(), task -> {

                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {

                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    String userId = documentSnapshot.getId();

                    Log.i(TAG, contact.getKey() + " ==> " + userId + " ==> " + contact.getValue());

                    Realm _realm = RealmUtil.getCustomRealmInstance(context);
                    _realm.executeTransaction(realmIns -> {

                        RavenUser ravenUser = realmIns.where(RavenUser.class).equalTo("userId", userId).findFirst();
                        if (ravenUser == null) {
                            ravenUser = new RavenUser();
                            ravenUser.setUserId(userId);
                        }

                        try {
                            ravenUser.cloneObject(documentSnapshot.toObject(User.class));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ravenUser.setContactName(contact.getValue());
                        ravenUser.setInContacts(true);

                        realmIns.insertOrUpdate(ravenUser);
                    });
                    _realm.close();

                } else {
                    Log.i(TAG, contact.getKey() + " ==> " + "doesn't exist.");

                    Realm _realm = RealmUtil.getCustomRealmInstance(context);
                    _realm.executeTransaction(realmIns -> realmIns.where(RavenUser.class).equalTo("phoneNumber", contact.getKey()).findAll().deleteAllFromRealm());
                    _realm.close();
                }
            });
        }
    }
}