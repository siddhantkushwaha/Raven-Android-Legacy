package com.siddhantkushwaha.raven.service

import android.content.Context
import android.util.Log
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.raven.NotificationSender
import com.siddhantkushwaha.raven.activity.ChatActivity
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import java.util.*

fun sendNewMessageNotification(context: Context, threadId: String, messageId: String) {

    ThreadManager().getMessageByMessageId(threadId, messageId) {
        if (it.isSuccessful) {

            val message = it.result?.toObject(Message::class.java)
                    ?: return@getMessageByMessageId
            decryptMessage(message, threadId)

            UserManager().getUserByUserId(message.sentByUserId) { it2 ->
                if (it2.isSuccessful) {

                    val user = it2.result?.toObject(User::class.java) ?: return@getUserByUserId
                    sendNotification(context, threadId, message, user)
                }
            }
        }
    }
}

private fun sendNotification(context: Context, threadId: String, message: Message, user: User) {

    if (ActivityInfo.getClassName() == ChatActivity::class.java.toString() && threadId == ActivityInfo.getIntentInfo().getString("threadId")) {
        Log.i(FirebaseCloudMessaging.TAG, "returning2");
        return
    }

    val requestCode = Random().nextInt(100000)
    val intent = ChatActivity.getIntent(context, ChatActivity.Companion.IntentData(threadId))
    val notificationSender = NotificationSender(context,
            threadId,
            requestCode,
            user.userProfile?.name ?: user.phoneNumber ?: "Message Notification",
            message.text,
            intent)

    notificationSender.sendNotificationWithReplyAction(message.sentByUserId, threadId, user.userProfile?.picUrl)
}

private fun decryptMessage(message: Message, threadId: String) {

    if (message.text == null && message.fileRef == null) {
        message.text = "Message Deleted."
    } else if (message.text == null) {
        message.text = "Photo."
    } else if (message.fileRef == null) {
        try {
            message.text = ThreadManager.decryptMessage(threadId, message.text)
        } catch (e: Exception) {
            message.text = "Couldn't Decrypt."
        }
    } else {
        try {
            message.text = ThreadManager.decryptMessage(threadId, message.text)
        } catch (e: Exception) {
            message.text = "Couldn't Decrypt."
        }
    }
}