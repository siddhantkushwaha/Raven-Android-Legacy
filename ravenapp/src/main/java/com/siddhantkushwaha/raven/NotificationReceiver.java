package com.siddhantkushwaha.raven;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.manager.ThreadManager;
import com.siddhantkushwaha.raven.utility.CurrentFirebaseUser;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_REPLY = "NOTIFICATION_REPLY";
    public static final String USER_ID = "USER_ID";
    public static final String THREAD_ID = "THREAD_ID";

    public static Intent getIntent(@NonNull Context context, @NonNull String userId, @NonNull String threadId) {

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(THREAD_ID, threadId);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(NotificationReceiver.class.toString(), "Sending message via notification.");

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {

            String threadId = intent.getStringExtra(THREAD_ID);
            String userId = intent.getStringExtra(USER_ID);

            CharSequence c = remoteInput.getCharSequence(NOTIFICATION_REPLY);
            String message = null;
            if (c != null) {
                message = c.toString();
            }

            if (message == null || threadId == null || userId == null)
                return;

            String senderUid = CurrentFirebaseUser.getUid();
            if (senderUid == null)
                return;

            String encryptedMessage = ThreadManager.encryptMessage(threadId, message);
            if (encryptedMessage == null)
                return;

            Message messageObject = new Message(encryptedMessage, Timestamp.now(), senderUid, userId);
            new ThreadManager().sendMessage(threadId, messageObject);

            NotificationSender.cancelNotification(context, threadId, 0);
        }
    }
}