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

    static final String TAG = FirebaseMessagingService.class.toString();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {

        Log.i(TAG, "MESSAGE_RECEIVED");
        sendMyNotificationV2(message.getData(), message.getFrom());
    }

    // TODO this is to support older versions than 1.0.6
    private void sendMyNotificationV1(Map<String, String> notification) {

        String title = notification.get("title");
        String message = notification.get("message");

        if (title == null || message == null)
            return;

        Log.i(TAG, "sendMyNotificationV1");

        Message messageObject = null;
        try {
            messageObject = GsonUtil.fromGson(message, Message.class);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
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

    // TODO this is to support older versions than 1.0.6
    private void setMessageText(String threadId, Message messageObject) {
        try {
            messageObject.setText(ThreadManager.decryptMessage(threadId, messageObject.getText()));
        } catch (Exception e) {
            messageObject.setText("Couldn't Decrypt.");
            e.printStackTrace();
        }
    }

    private void sendMyNotificationV2(Map<String, String> notification, String topic) {

        String threadId = notification.get("threadId");
        String messageId = notification.get("messageId");

        if (threadId == null || messageId == null) {

            sendMyNotificationV1(notification);
            return;
        }

        Log.i(TAG, "sendMyNotificationV2");

        if (FirebaseAuth.getInstance().getUid() != null && !topic.replace("/topics/", "").equals(FirebaseAuth.getInstance().getUid())) {
            Log.i(TAG, "returning1 " + topic);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            return;
        }

        FirebaseCloudMessaginUtilKt.sendNewMessageNotification(this, threadId, messageId);
    }
}