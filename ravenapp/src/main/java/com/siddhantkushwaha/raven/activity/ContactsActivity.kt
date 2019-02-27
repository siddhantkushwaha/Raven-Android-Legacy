package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.raven.BuildConfig
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.main.ChatActivity
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.utility.RavenUtils
import kotlinx.android.synthetic.main.activity_base_raven_user.*
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : BaseRavenUserActivity(R.layout.activity_contacts) {

    companion object {
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, ContactsActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar.title = "Contacts"

        if (BuildConfig.DEBUG)
            newGroup.visibility = View.VISIBLE
        else
            newGroup.visibility = View.GONE


        newGroup.setOnClickListener {
            NewGroupActivity.openActivity(this@ContactsActivity, false)
        }

        userListView.setOnItemClickListener { _, _, position, _ ->

            val userId = (userListView.adapter as ContactAdapter).getItem(position)?.userId
                    ?: return@setOnItemClickListener
            val threadId = RavenUtils.getThreadId(FirebaseAuth.getInstance().uid, userId)
            ChatActivity.openActivity(this@ContactsActivity, false,
                    ChatActivity.Companion.IntentData(threadId))
        }
    }
}