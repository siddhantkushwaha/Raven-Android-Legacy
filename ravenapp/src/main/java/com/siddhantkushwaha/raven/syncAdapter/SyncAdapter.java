package com.siddhantkushwaha.raven.syncAdapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ContactsUtil;
import com.siddhantkushwaha.raven.manager.UserManager;
import com.siddhantkushwaha.raven.realm.entity.RavenUser;
import com.siddhantkushwaha.raven.realm.utility.RavenUserUtil;
import com.siddhantkushwaha.raven.utility.RealmUtil;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmResults;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.toString();
    private Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;

        if (FirebaseApp.getApps(this.context).isEmpty()) {
            FirebaseApp.initializeApp(this.context, FirebaseOptions.fromResource(this.context));
        }

        Fabric.with(context, new Crashlytics());
        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        Crashlytics.setUserName(FirebaseAuth.getInstance().getUid());
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        syncContacts(context);
    }

    public static void syncContacts(Context context) {

        Realm realm = RealmUtil.getCustomRealmInstance(context);

        HashMap<String, String> contactsList = ContactsUtil.getAllContacts(context);

        realm.executeTransactionAsync(realmL -> {
            RealmResults<RavenUser> realmResults = realmL.where(RavenUser.class).findAll();
            for (RavenUser contact : realmResults) {
                if (!contactsList.containsKey(contact.phoneNumber)) {
                    RavenUser ravenUser = realmL.where(RavenUser.class).equalTo("phoneNumber", contact.phoneNumber).findFirst();
                    if (ravenUser != null) {
                        ravenUser.contactName = null;
                        realmL.insertOrUpdate(ravenUser);
                    }
                }
            }
        });

        UserManager userManager = new UserManager();
        for (Map.Entry<String, String> contact : contactsList.entrySet()) {
            userManager.startGetUserByAttribute(UserManager.KEY_PHONE, contact.getKey(), task -> {

                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {

                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    String userId = documentSnapshot.getId();

                    RavenUserUtil.setUser(realm, true, userId, documentSnapshot, null, true, contact.getValue());
                } else
                    RavenUserUtil.deleteByPhoneNumber(realm, contact.getKey());
            });
        }
    }
}