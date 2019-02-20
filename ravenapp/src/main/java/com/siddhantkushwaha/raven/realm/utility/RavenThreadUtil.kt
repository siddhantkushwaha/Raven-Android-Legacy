package com.siddhantkushwaha.raven.realm.utility

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
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

            ravenThread.backgroundFileRef = thread.backgroundMetadata?.fileRef
            ravenThread.backgroundOpacity = thread.backgroundMetadata?.opacity
        }

        @JvmStatic
        fun setThread(realm: Realm, threadId: String, userId: String, threadSnap: DocumentSnapshot? = null, e: FirebaseFirestoreException? = null) {

            val thread = threadSnap?.toObject(Thread::class.java)
            realm.executeTransactionAsync { realmL ->
                var ravenThread = realmL.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                if (threadSnap != null && threadSnap.exists() && thread != null) {
                    if (ravenThread == null) {
                        ravenThread = RavenThread()
                        ravenThread.threadId = threadId
                        ravenThread.userId = userId
                    }

                    clone(ravenThread, thread)
                    addUsers(realmL, ravenThread, thread.users)

                    realmL.insertOrUpdate(ravenThread)
                } else {
                    ravenThread?.deleteFromRealm()
                }
            }
            e?.printStackTrace()
        }

        @JvmStatic
        fun setLastMessage(realm: Realm, threadId: String, messageId: String = "NoMessage") {

            realm.executeTransactionAsync { realmL ->
                val ravenThread = realmL.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ?: return@executeTransactionAsync
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
        }

        @JvmStatic
        fun addUsers(realmL: Realm, ravenThread: RavenThread, userIds: ArrayList<String>) {

            if(ravenThread.users == null)
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
    }
}