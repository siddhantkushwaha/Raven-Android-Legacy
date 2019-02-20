package com.siddhantkushwaha.raven.realm.utility

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.siddhantkushwaha.nuttertools.GsonUtil
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.realm.entity.RavenMessage
import io.realm.Realm
import io.realm.RealmList
import org.joda.time.DateTime

class RavenMessageUtil {

    companion object {

        @JvmStatic
        fun clone(ravenMessage: RavenMessage, message: Message) {

            ravenMessage.text = message.text
            ravenMessage.fileRef = message.fileRef
            ravenMessage.sentByUserId = message.sentByUserId

            if (message.sentTo != null) {
                val arr = RealmList<String>()
                arr.addAll(message.sentTo)
                ravenMessage.sentTo = arr
            }

            ravenMessage.localTimestamp = DateTime(message.sentTime.toDate()).toString()

            if (message.timestamp != null)
                ravenMessage.timestamp = DateTime(message.timestamp.toDate()).toString()

            if (message.notDeletedBy != null) {
                val arr = RealmList<String>()
                arr.addAll(message.notDeletedBy)
                ravenMessage.notDeletedBy = arr

                val currUserId = FirebaseAuth.getInstance().uid
                ravenMessage.deletedBy = if (arr.contains(currUserId)) null else currUserId
            }

            if (message.seenBy != null) {
                val map = HashMap<String, String>()
                message.seenBy.forEach { pair ->
                    map[pair.key] = DateTime(pair.value.toDate()).toString()
                }
                ravenMessage.seenBy = GsonUtil.toGson(map)
            }
        }

        @JvmStatic
        fun setMessage(realm: Realm, threadId: String, messageId: String, messageSnap: DocumentSnapshot? = null, e: FirebaseFirestoreException? = null) {

            val message = messageSnap?.toObject(Message::class.java)
            realm.executeTransactionAsync { realmL ->
                var ravenMessage = realmL.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()
                if (messageSnap != null && messageSnap.exists() && message != null) {
                    if (ravenMessage == null) {
                        ravenMessage = RavenMessage()
                        ravenMessage.threadId = threadId
                        ravenMessage.messageId = messageId
                    }

                    clone(ravenMessage, message)

                    realmL.insertOrUpdate(ravenMessage)
                } else
                    ravenMessage?.deleteFromRealm()
            }
            e?.printStackTrace()
        }
    }
}