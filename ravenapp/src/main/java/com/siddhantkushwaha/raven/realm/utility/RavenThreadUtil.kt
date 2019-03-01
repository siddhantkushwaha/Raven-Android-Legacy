package com.siddhantkushwaha.raven.realm.utility

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.siddhantkushwaha.nuttertools.GsonUtil
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.Thread
import com.siddhantkushwaha.raven.realm.entity.RavenMessage
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import io.realm.Realm
import io.realm.RealmList

class RavenThreadUtil {

    companion object {

        @JvmStatic
        fun clone(ravenThread: RavenThread, thread: Thread) {

            ravenThread.groupName = thread.groupDetails?.name
            ravenThread.picUrl = thread.groupDetails?.picUrl

            ravenThread.permissions = GsonUtil.toGson(thread.groupDetails?.permissions)

            ravenThread.backgroundFileRef = thread.backgroundMetadata?.fileRef
            ravenThread.backgroundOpacity = thread.backgroundMetadata?.opacity
        }

        @JvmStatic
        fun setThread(realm: Realm, performAsync: Boolean, threadId: String, userId: String, threadSnap: DocumentSnapshot? = null, e: FirebaseFirestoreException? = null) {

            e?.printStackTrace()

            val thread = threadSnap?.toObject(Thread::class.java)
            val transaction = Realm.Transaction { realmL ->
                var ravenThread = realmL.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                if (threadSnap != null && threadSnap.exists() && thread != null) {
                    if (ravenThread == null) {
                        ravenThread = RavenThread()
                        ravenThread.threadId = threadId
                    }

                    ravenThread.userId = userId

                    clone(ravenThread, thread)
                    addUsers(realmL, ravenThread, thread.users ?: ArrayList())

                    realmL.insertOrUpdate(ravenThread)
                } else {
                    ravenThread?.deleteFromRealm()
                }
            }

            if (performAsync)
                realm.executeTransactionAsync(transaction)
            else
                realm.executeTransaction(transaction)
        }

        @JvmStatic
        fun setLastMessage(realm: Realm, performAsync: Boolean, threadId: String, messageId: String = "NoMessage") {

            val transaction = Realm.Transaction { realmL ->
                val ravenThread = realmL.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ?: return@Transaction
                var ravenMessage = realmL.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()

                if (messageId != "NoMessage" && ravenMessage == null) {
                    ravenMessage = RavenMessage()
                    ravenMessage.threadId = threadId
                    ravenMessage.messageId = messageId
                    ravenMessage = realmL.copyToRealmOrUpdate(ravenMessage)
                }

                ravenThread.lastMessage = ravenMessage

                if (ravenMessage != null)
                    ravenThread.timestamp = ravenMessage.timestamp ?: ravenMessage.localTimestamp

                realmL.insertOrUpdate(ravenThread)
            }

            if (performAsync)
                realm.executeTransactionAsync(transaction)
            else
                realm.executeTransaction(transaction)
        }

        @JvmStatic
        fun addUsers(realmL: Realm, ravenThread: RavenThread, userIds: ArrayList<String>) {

            if (ravenThread.users == null)
                ravenThread.users = RealmList()

            val arr = ArrayList<RavenUser>()
            arr.addAll(ravenThread.users)
            for (ru1 in arr) {
                if (userIds.findLast { userId -> ru1.userId == userId } == null) {
                    ravenThread.users.removeAll { ru2 ->
                        ru2.userId == ru1.userId
                    }
                }
            }

            userIds.forEach { userId ->

                if (ravenThread.users.findLast { ru -> userId == ru.userId } == null) {
                    var ru = realmL.where(RavenUser::class.java).equalTo("userId", userId).findFirst()
                    if (ru == null) {
                        ru = RavenUser()
                        ru.userId = userId
                        ru = realmL.copyToRealmOrUpdate(ru)
                    }
                    ravenThread.users.add(ru)
                }
            }

        }

        @JvmStatic
        fun getMessagesByUserId(userId: String, messages: HashMap<String, Message>?): Map<String, Message>? {
            return messages?.filter { me ->
                me.value.notDeletedBy.contains(userId)
            }
        }

        @JvmStatic
        fun findMostRecentMessage(userId: String, messages: HashMap<String, Message>?): MutableMap.MutableEntry<String, Message>? {

            return messages?.entries?.sortedBy { me ->
                me.value.timestamp
            }?.findLast { me -> me.value.notDeletedBy.contains(userId) }
        }
    }
}