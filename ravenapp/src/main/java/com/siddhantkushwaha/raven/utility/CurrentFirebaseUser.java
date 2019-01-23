package com.siddhantkushwaha.raven.utility;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CurrentFirebaseUser {

    public static FirebaseUser getUser() {

        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUid() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return null;
        else
            return user.getUid();
    }

    public static String getEmail() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return null;
        else
            return user.getEmail();
    }

    public static String getPhoneNumber() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return null;
        else
            return user.getPhoneNumber();
    }
}
