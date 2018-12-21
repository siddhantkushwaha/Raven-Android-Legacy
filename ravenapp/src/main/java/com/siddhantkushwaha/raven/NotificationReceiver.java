package com.siddhantkushwaha.raven;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.manager.ThreadManager;
import com.siddhantkushwaha.raven.ravenUtility.CurrentFirebaseUser;

import org.joda.time.DateTime;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(NotificationReceiver.class.toString(), "Sending message via notification.");

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {

            String threadId = intent.getStringExtra(context.getString(R.string.key_thread_id));
            String userId = intent.getStringExtra(context.getString(R.string.key_user_id));

            CharSequence c = remoteInput.getCharSequence(context.getString(R.string.key_notification_reply));
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