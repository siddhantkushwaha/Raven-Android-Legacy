package com.siddhantkushwaha.raven;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.manager.ThreadManager;
import com.siddhantkushwaha.raven.utility.RavenUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_REPLY = "NOTIFICATION_REPLY";
    public static final String THREAD_ID = "THREAD_ID";

    public static Intent getIntent(@NonNull Context context, @NonNull String threadId) {

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(THREAD_ID, threadId);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(NotificationReceiver.class.toString(), "Sending message via notification.");

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {

            String threadId = intent.getStringExtra(THREAD_ID);

            String senderUid = FirebaseAuth.getInstance().getUid();
            if (senderUid == null)
                return;

            String userId = RavenUtils.getUserId(threadId, senderUid);
            if (userId.equals(RavenUtils.GROUP))
                return;

            CharSequence c = remoteInput.getCharSequence(NOTIFICATION_REPLY);
            String message = null;
            if (c != null) {
                message = c.toString();
            }

            if (message == null)
                return;

            String encryptedMessage = ThreadManager.encryptMessage(threadId, message);
            if (encryptedMessage == null)
                return;

            // TODO this is not for groups, change in future

            ArrayList<String> notDeletedBy = new ArrayList<>();
            ArrayList<String> sentTo = new ArrayList<>();

            notDeletedBy.add(userId);
            sentTo.add(userId);

            notDeletedBy.add(senderUid);

            Message messageObject = new Message();
            messageObject.setText(encryptedMessage);
            messageObject.setSentByUserId(senderUid);
            messageObject.setSentTime(Timestamp.now());
            messageObject.setNotDeletedBy(notDeletedBy);
            messageObject.setSentTo(sentTo);
            new ThreadManager().sendMessage(threadId, messageObject, userId);

            NotificationSender.cancelNotification(context, threadId, 0);
        }
    }
}