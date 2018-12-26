package com.siddhantkushwaha.raven.service;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.siddhantkushwaha.raven.NotificationSender;
import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.activity.ChatActivity;
import com.siddhantkushwaha.raven.commonUtility.ActivityInfo;
import com.siddhantkushwaha.raven.commonUtility.GsonUtils;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.manager.ThreadManager;
import com.siddhantkushwaha.raven.ravenUtility.CurrentFirebaseUser;
import com.siddhantkushwaha.raven.ravenUtility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.ravenUtility.RavenUtils;

import java.util.Map;
import java.util.Random;

public class FirebaseCloudMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {

        Log.i(FirebaseCloudMessaging.class.toString(), "MESSAGE_RECEIVED");
        sendMyNotification(message.getData());
    }

    private void sendMyNotification(Map<String, String> notification) {

        String title = notification.get("title");
        String message = notification.get("message");

        if (title == null || message == null)
            return;

        Message messageObject = null;
        try {
            messageObject = GsonUtils.fromGson(message, Message.class);
        } catch (Exception e) {
            Log.e(FirebaseCloudMessaging.class.toString(), e.toString());
        }

        if (messageObject == null)
            return;


        String currentUserId = CurrentFirebaseUser.getUid();
        if (currentUserId == null || !currentUserId.equals(messageObject.getSentToUserId())) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(messageObject.getSentToUserId());
            return;
        }

        if (ActivityInfo.getClassName() != null && ChatActivity.class.toString().equals(ActivityInfo.getClassName())) {
            if (ActivityInfo.getIntentInfo() != null && messageObject.getSentByUserId().equals(ActivityInfo.getIntentInfo().getString(this.getString(R.string.key_user_id)))) {
                return;
            }
        }

        String threadId = RavenUtils.getThreadId(messageObject.getSentToUserId(), messageObject.getSentByUserId());
        messageObject = ThreadManager.decryptMessage(threadId, messageObject);

        Random rand = new Random();
        int requestCode = rand.nextInt(100000);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(getString(R.string.key_user_id), messageObject.getSentByUserId());

        NotificationSender notificationSender = new NotificationSender(FirebaseCloudMessaging.this, threadId, requestCode, title, messageObject.getText(), intent);
        notificationSender.sendNotificationWithReplyAction(messageObject.getSentByUserId(), threadId, "REPLY");
    }
}