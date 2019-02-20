package com.siddhantkushwaha.raven.realm.entity

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

class RavenMessageKt : RealmObject() {

    @PrimaryKey
    var messageId: String? = null

    val threadId: String? = null

    val text: String? = null
    val fileRef: String? = null

    val sentByUserId: String? = null
    val sentTo: RealmList<String>? = null

    val timestamp: String? = null
    val localTimestamp: String? = null

    val seenBy: String? = null

    val deletedBy: String? = null
    val notDeletedBy: RealmList<String>? = null

    val selected: Boolean = false
}