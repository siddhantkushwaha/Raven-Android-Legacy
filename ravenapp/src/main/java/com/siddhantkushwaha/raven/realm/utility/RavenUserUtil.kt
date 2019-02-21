package com.siddhantkushwaha.raven.realm.utility

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import io.realm.Realm

class RavenUserUtil {

    companion object {

        @JvmStatic
        fun clone(ravenUser: RavenUser, user: User) {

            ravenUser.phoneNumber = user.phoneNumber
            ravenUser.displayName = user.userProfile?.name
            ravenUser.about = user.userProfile?.about
            ravenUser.picUrl = user.userProfile?.picUrl
        }

        @JvmStatic
        fun setUser(realm: Realm, userId: String, userSnap: DocumentSnapshot?, e: FirebaseFirestoreException?, updateContactName: Boolean = false, contactName: String? = null) {

            val user = userSnap?.toObject(User::class.java)
            realm.executeTransactionAsync { realmL ->
                var ravenUser = realmL.where(RavenUser::class.java).equalTo("userId", userId).findFirst()
                if (userSnap != null && userSnap.exists() && user != null) {
                    if (ravenUser == null) {
                        ravenUser = RavenUser()
                        ravenUser.userId = userId
                    }

                    clone(ravenUser, user)

                    if (updateContactName)
                        ravenUser.contactName = contactName

                    realmL.insertOrUpdate(ravenUser)
                } else {
                    ravenUser?.deleteFromRealm()
                }
            }
            e?.printStackTrace()
        }

        @JvmStatic
        fun deleteByPhoneNumber(realm: Realm, phoneNumber: String) {

            realm.executeTransactionAsync {
                it.where(RavenUser::class.java).equalTo("phoneNumber", phoneNumber).findAll().deleteAllFromRealm()
            }
        }
    }
}