package com.siddhantkushwaha.raven.realm.utility

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.siddhantkushwaha.nuttertools.GsonUtil
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.realm.entity.RavenMessage
import com.siddhantkushwaha.raven.utility.JodaTimeUtilV2
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

            ravenMessage.localTimestamp = JodaTimeUtilV2.getDateTimeAsString(message.sentTime)
            ravenMessage.timestamp = JodaTimeUtilV2.getDateTimeAsString(message.timestamp)

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
                    map[pair.key] = JodaTimeUtilV2.getDateTimeAsString(pair.value)
                }
                ravenMessage.seenBy = GsonUtil.toGson(map)
            }

            ravenMessage.uploadUri = null
        }

        @JvmStatic
        fun setMessage(realm: Realm, performAsync: Boolean, threadId: String, messageId: String, message: Message? = null, e: FirebaseFirestoreException? = null) {

            e?.printStackTrace()

            val transaction = Realm.Transaction { realmL ->
                var ravenMessage = realmL.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()
                if (message != null) {
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

            if (performAsync)
                realm.executeTransactionAsync(transaction)
            else
                realm.executeTransaction(transaction)
        }

        @JvmStatic
        fun revClone(ravenMessage: RavenMessage): Message {

            val message = Message()

            message.text = ravenMessage.text
            message.fileRef = ravenMessage.fileRef
            message.sentByUserId = ravenMessage.sentByUserId

            message.sentTime = Timestamp(DateTime.parse(ravenMessage.localTimestamp).toDate())

            message.notDeletedBy = ArrayList()
            ravenMessage.notDeletedBy?.forEach {
                message.notDeletedBy.add(it)
            }

            return message
        }
    }
}