package com.siddhantkushwaha.raven.commonUtility;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.HashMap;

public class ContactsUtil {

    public static boolean contactsReadPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
    }

    public static void getReadContactsPermission(Activity activity) {

        if (!contactsReadPermission(activity)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    0);
        }
    }

    public static HashMap<String, String> getAllContacts(Context context) {

        HashMap<String, String> contactsList = new HashMap<>();

        if (!contactsReadPermission(context))
            return contactsList;

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if(phoneNumber == null)
                    continue;

                contactsList.put(phoneNumber, name);
            }
            cursor.close();
        }
        return contactsList;
    }
}
