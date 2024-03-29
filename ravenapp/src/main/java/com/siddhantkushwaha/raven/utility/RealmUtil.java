package com.siddhantkushwaha.raven.utility;


import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtil {

    public static Realm getCustomRealmInstance(Context context) {

        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("raven_realm.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        return Realm.getInstance(config);
    }

    public static void clearData(Realm realm) {

        realm.executeTransaction(realmL -> realmL.deleteAll());
    }
}
