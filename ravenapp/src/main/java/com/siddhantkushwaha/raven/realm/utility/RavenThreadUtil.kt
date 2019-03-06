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
        fun setLastMessage(realm: Realm, performAsync: Boolean, threadId: String, userId: String) {

            val transaction = Realm.Transaction { realmL ->

                val rt = realmL.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ?: return@Transaction

                val allMsg = realmL.where(RavenMessage::class.java).equalTo("threadId", threadId).findAll()
                val lastMessage = allMsg.sortedBy { rm ->
                    rm.localTimestamp
                }.findLast { rm ->
                    rm.notDeletedBy?.contains(userId) == true
                }

                rt.lastMessage = lastMessage

                val threadTimestamp = lastMessage?.timestamp ?: lastMessage?.localTimestamp
                if (threadTimestamp != null) {
                    rt.timestamp = threadTimestamp
                }

                realmL.insertOrUpdate(rt)
            }

            if (performAsync)
                realm.executeTransactionAsync(transaction)
            else
                realm.executeTransaction(transaction)
        }
    }
}