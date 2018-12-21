package com.siddhantkushwaha.raven.commonUtility;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.siddhantkushwaha.raven.localEntity.RavenUser;

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

    public static HashMap<String, RavenUser> getAllContacts(Context context) {

        HashMap<String, RavenUser> contactsList = new HashMap<>();

        if (!contactsReadPermission(context))
            return contactsList;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                if (phoneCursor != null) {
                    while (phoneCursor.moveToNext()) {

                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));

                        if (phoneNumber == null)
                            continue;

                        RavenUser ravenUser = new RavenUser();
                        ravenUser.setContactName(name);
                        ravenUser.setPhoneNumber(phoneNumber);

                        contactsList.put(phoneNumber, ravenUser);
                    }
                    phoneCursor.close();
                }
            }
            cursor.close();
        }

        return contactsList;
    }

    
}
