package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.main.ChatActivity
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.activity_new_group.*
import kotlinx.android.synthetic.main.activity_base_raven_user.*

class NewGroupActivity : BaseRavenUserActivity(R.layout.activity_new_group) {

    companion object {
        val tag = NewGroupActivity::class.java.toString()
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, NewGroupActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar.title = "New Group"

        done.setOnClickListener {

            val groupName = "New Group"
            createGroup(groupName)
        }

        contactsChangeListener = RealmChangeListener {

            contactsAdapter.notifyDataSetChanged()

            if (getSelected().isNotEmpty()) {
                done.visibility = View.VISIBLE
            } else {
                done.visibility = View.GONE
            }
        }

        userListView.setOnItemClickListener { _, _, position, _ ->

            val userId = (userListView.adapter as ContactAdapter).getItem(position)?.userId
                    ?: return@setOnItemClickListener

            realm.executeTransaction { realmL ->
                val ru = realmL.where(RavenUser::class.java).equalTo("userId", userId).findFirst()
                        ?: return@executeTransaction
                ru.selected = !ru.selected
                realmL.insertOrUpdate(ru)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun createGroup(groupName: String) {

        val users = getSelected()
        if (users.isEmpty())
            return
        val userIds = ArrayList<String>()
        for (ru in users) {
            userIds.add(ru.userId)
        }

        val myUserId = FirebaseAuth.getInstance().uid!!
        if (!userIds.contains(myUserId))
            userIds.add(myUserId)

        val permissions = HashMap<String, String>()
        permissions[FirebaseAuth.getInstance().uid!!] = "admin"

        ThreadManager().createGroup(groupName, userIds, permissions) { task ->
            val threadId = task.result?.id
            if (task.isSuccessful && threadId != null) {

                Toast.makeText(this@NewGroupActivity, "Group $groupName created.", Toast.LENGTH_LONG).show()
                ChatActivity.openActivity(this@NewGroupActivity, true, ChatActivity.Companion.IntentData(threadId))
            }
        }
    }
}
