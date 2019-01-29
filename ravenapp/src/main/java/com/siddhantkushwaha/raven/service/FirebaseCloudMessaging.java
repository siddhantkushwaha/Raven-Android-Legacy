package com.siddhantkushwaha.raven.service;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo;
import com.siddhantkushwaha.nuttertools.GsonUtil;
import com.siddhantkushwaha.raven.NotificationSender;
import com.siddhantkushwaha.raven.activity.ChatActivity;
import com.siddhantkushwaha.raven.entity.Message;
import com.siddhantkushwaha.raven.manager.ThreadManager;
import com.siddhantkushwaha.raven.utility.RavenUtils;

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
            messageObject = GsonUtil.fromGson(message, Message.class);
        } catch (Exception e) {
            Log.e(FirebaseCloudMessaging.class.toString(), e.toString());
        }

        if (messageObject == null)
            return;


        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || !currentUserId.equals(messageObject.getSentToUserId())) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(messageObject.getSentToUserId());
            return;
        }

        String threadId = RavenUtils.getThreadId(messageObject.getSentToUserId(), messageObject.getSentByUserId());

        if (ActivityInfo.getClassName() != null && ChatActivity.class.toString().equals(ActivityInfo.getClassName())) {
            if (ActivityInfo.getIntentInfo() != null && threadId.equals(ActivityInfo.getIntentInfo().getString("threadId"))) {
                return;
            }
        }

        if (messageObject.getText() == null && messageObject.getFileRef() == null) {
            messageObject.setText("Message Deleted.");
        } else if (messageObject.getText() == null) {
            messageObject.setText("Photo.");
        } else if (messageObject.getFileRef() == null) {
            setMessageText(threadId, messageObject);
        } else {
            setMessageText(threadId, messageObject);
        }

        Random rand = new Random();
        int requestCode = rand.nextInt(1000000);

        Intent intent = ChatActivity.getIntent(this, new ChatActivity.Companion.IntentData(threadId));
        NotificationSender notificationSender = new NotificationSender(FirebaseCloudMessaging.this, threadId, requestCode, title, messageObject.getText(), intent);
        notificationSender.sendNotificationWithReplyAction(messageObject.getSentByUserId(), threadId, "REPLY");
    }

    private void setMessageText(String threadId, Message messageObject) {
        try {
            messageObject.setText(ThreadManager.decryptMessage(threadId, messageObject.getText()));
        } catch (Exception e) {
            messageObject.setText("Couldn't Decrypt.");
            e.printStackTrace();
        }
    }
}