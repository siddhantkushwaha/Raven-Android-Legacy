package com.siddhantkushwaha.raven.service

import android.content.Context
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.raven.NotificationSender
import com.siddhantkushwaha.raven.activity.main.ChatActivity
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.Thread
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.utility.FirebaseUtils
import com.siddhantkushwaha.raven.utility.RavenUtils
import java.util.*

fun sendNewMessageNotification(context: Context, threadId: String, messageId: String) {

    FirebaseUtils.getFirestoreDb(true).collection("threads").document(threadId).get().addOnCompleteListener { task1 ->
        val thread = task1.result?.toObject(Thread::class.java)
        val message = thread?.messages?.get(messageId)
        if (message != null) {
            UserManager().getUserByUserId(message.sentByUserId) { task2 ->
                val userId = task2.result?.id
                val user = task2.result?.toObject(User::class.java)
                if (userId != null && user != null) {
                    sendNotification(context, threadId, thread, messageId, message, userId, user)
                }
            }
        }
    }
}

private fun sendNotification(context: Context, threadId: String, thread: Thread, messageId: String, message: Message, userId: String, user: User) {

    if (ActivityInfo.getClassName() == ChatActivity::class.java.toString() && threadId == ActivityInfo.getIntentInfo().getString("threadId"))
        return

    val requestCode = Random().nextInt(100000)
    val intent = ChatActivity.getIntent(context, ChatActivity.Companion.IntentData(threadId))

    val isGroup = RavenUtils.isGroup(threadId, userId)

    setMessageText(message, threadId)

    val notificationSender = NotificationSender(context)

    notificationSender.sendNotificationWithReplyAction(requestCode, threadId, thread.groupDetails?.name, user.userProfile?.name
            ?: user.phoneNumber, if (isGroup) thread.groupDetails?.picUrl else user.userProfile?.picUrl, message.text, isGroup, thread.users, intent)
}

private fun setMessageText(message: Message, threadId: String) {

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