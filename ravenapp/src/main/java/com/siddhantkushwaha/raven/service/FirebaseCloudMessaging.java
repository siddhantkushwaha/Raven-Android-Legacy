package com.siddhantkushwaha.raven.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

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

    private void sendMyNotificationV2(Map<String, String> notification, String topic) {

        String threadId = notification.get("threadId");
        String messageId = notification.get("messageId");

        if (threadId == null || messageId == null) {
            return;
        }

        Log.i(TAG, "sendMyNotificationV2");

        if (FirebaseAuth.getInstance().getUid() != null && !topic.replace("/topics/", "").equals(FirebaseAuth.getInstance().getUid())) {
            Log.i(TAG, "returning1 " + topic);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            return;
        }

        Log.i(TAG, "HERE");

        FirebaseCloudMessaginUtilKt.sendNewMessageNotification(this, threadId, messageId);
    }
}