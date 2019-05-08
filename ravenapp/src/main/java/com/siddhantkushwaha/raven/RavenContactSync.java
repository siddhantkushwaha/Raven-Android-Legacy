package com.siddhantkushwaha.raven;

import android.content.Context;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.siddhantkushwaha.raven.manager.UserManager;
import com.siddhantkushwaha.raven.realm.entity.RavenUser;
import com.siddhantkushwaha.raven.realm.utility.RavenUserUtil;
import com.siddhantkushwaha.raven.utility.ContactsUtil;
import com.siddhantkushwaha.raven.utility.RealmUtil;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class RavenContactSync {

    public static void syncContacts(Context context) {

        HashMap<String, String> contactsList = ContactsUtil.getAllContacts(context);
        if (contactsList == null)
            return;

        Realm realm = RealmUtil.getCustomRealmInstance(context);
        try {
            realm.executeTransaction(realmL -> {
                RealmResults<RavenUser> realmResults = realmL.where(RavenUser.class).isNotNull("contactName").findAll();
                for (RavenUser contact : realmResults) {
                    if (!contactsList.containsKey(contact.phoneNumber)) {
                        contact.contactName = null;
                        realmL.insertOrUpdate(contact);
                    }
                }
            });
        } catch (Exception e) {
            reportException(context, e);
        }

        UserManager userManager = new UserManager();
        for (Map.Entry<String, String> contact : contactsList.entrySet()) {
            userManager.startGetUserByAttribute(UserManager.KEY_PHONE, contact.getKey(), task -> {

                QuerySnapshot querySnapshot = task.getResult();

                try {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {

                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String userId = documentSnapshot.getId();

                        RavenUserUtil.setUser(realm, false, userId, documentSnapshot, null, true, contact.getValue());
                    } else
                        RavenUserUtil.setContactName(realm, false, contact.getKey(), null);
                } catch (Exception e) {
                    reportException(context, e);
                }
            });
        }
    }

    public static void reportException(Context context, Exception e) {
        e.printStackTrace();

        Crashlytics.log(1000, "UID", FirebaseAuth.getInstance().getUid());
        Crashlytics.logException(e);

        Toast.makeText(context, "Please contact Siddhant if you are seeing this message. " +
                "This in an important bug and needs to be fixed.", Toast.LENGTH_LONG).show();
    }
}