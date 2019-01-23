package com.siddhantkushwaha.raven;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;

import com.siddhantkushwaha.raven.common.utility.ContactsUtil;

public class RavenContactSync {

    public static final String ACCOUNT_TYPE = "com.siddhantkushwaha.raven.datasync";
    public static final String ACCOUNT = "Raven Account";
    public static final String AUTHORITY = "com.siddhantkushwaha.raven.provider";

    private static Uri mUri;
    private static Account mAccount;
    private static ContentResolver mResolver;

//     TODO PERIODIC SYNC
//     public static final long SYNC_INTERVAL = 60L;

    public class TableObserver extends ContentObserver {

        TableObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {

            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri changeUri) {

            ContentResolver.requestSync(mAccount, AUTHORITY, Bundle.EMPTY);
        }
    }

    public static Account CreateSyncAccount(Context context) {

        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        if (accountManager != null) {
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {

                ContentResolver.setIsSyncable(newAccount, AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);
            }
        }

        return newAccount;
    }

    public static void setupSync(Activity activity) {
        if (ContactsUtil.contactsReadPermission(activity)) {

            mAccount = CreateSyncAccount(activity);
            mResolver = activity.getContentResolver();
            mUri = ContactsContract.Contacts.CONTENT_URI;
            // HomeActivity.TableObserver observer = new HomeActivity.TableObserver(new Handler());
            // mResolver.registerContentObserver(mUri, true, observer);
            // ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);
            // ContentResolver.requestSync(mAccount, AUTHORITY, Bundle.EMPTY);
        }
    }

    public static void reSync(Context context) {

        ContentResolver.requestSync(CreateSyncAccount(context), AUTHORITY, Bundle.EMPTY);
    }
}