package com.siddhantkushwaha.raven.utility;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.HashMap;

public class ContactsUtil {

    public static boolean contactsReadPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static HashMap<String, String> getAllContacts(Context context) {

        if (!contactsReadPermission(context))
            return null;

        HashMap<String, String> contactsList = new HashMap<>();
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (phoneNumber == null)
                    continue;

                contactsList.put(phoneNumber, name);
            }
            cursor.close();
        }
        return contactsList;
    }
}