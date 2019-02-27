package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.main.ChatActivity
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import com.siddhantkushwaha.raven.utility.RealmUtil
import io.realm.*
import kotlinx.android.synthetic.main.activity_new_group.*

class NewGroupActivity : AppCompatActivity() {

    companion object {
        val tag = NewGroupActivity::class.java.toString()
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, NewGroupActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    private lateinit var realm: Realm
    private lateinit var allContacts: RealmResults<RavenUser>
    private lateinit var contactsAdapter: ContactAdapter
    private lateinit var contactsChangeListener: RealmChangeListener<RealmResults<RavenUser>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        realm = RealmUtil.getCustomRealmInstance(this)
        deselectAll()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchView.setIconifiedByDefault(true)
        searchView.maxWidth = android.R.attr.width

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean {
                filter(p0)
                return false
            }

            override fun onQueryTextChange(p0: String): Boolean {
                filter(p0)
                return false
            }

        })

        searchView.setOnCloseListener {
            userListView.adapter = contactsAdapter
            false
        }

        createGroup.setOnClickListener {
            if (groupName.text.isNotBlank()) {
                // create group
                val groupName = groupName.text.toString().trim()
                createGroup(groupName)
            } else
                Toast.makeText(this@NewGroupActivity, "Group name cannot be empty.", Toast.LENGTH_LONG).show()
        }

        userListView.emptyView = emptyView

        allContacts = realm.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).sort("contactName", Sort.ASCENDING).findAllAsync()

        contactsChangeListener = RealmChangeListener {
            contactsAdapter.notifyDataSetChanged()

            if (getSelected().isNotEmpty()) {
                createGroup.visibility = View.VISIBLE
            } else {
                createGroup.visibility = View.GONE
            }
        }

        contactsAdapter = ContactAdapter(this@NewGroupActivity, allContacts)

        userListView.adapter = contactsAdapter
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

    override fun onStart() {
        super.onStart()

        allContacts.addChangeListener(contactsChangeListener)
    }

    override fun onStop() {
        super.onStop()

        allContacts.removeAllChangeListeners()
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filter(query: String) {

        val regex = "*$query*"
        val searchResults = realm.where(RavenUser::class.java).isNotNull("contactName").like("contactName", regex, Case.INSENSITIVE).sort("contactName", Sort.ASCENDING).findAll()
        val searchAdapter = ContactAdapter(this@NewGroupActivity, searchResults)
        userListView.adapter = searchAdapter
    }

    private fun getSelected(): List<RavenUser> {
        return allContacts.filter { ru ->
            ru.selected
        }
    }

    private fun deselectAll() {

        realm.executeTransactionAsync { realmL ->
            realmL.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).findAll().forEach { ru ->
                ru.selected = false
                realmL.insertOrUpdate(ru)
            }
        }
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

        ThreadManager().createGroup(groupName, userIds) { task ->
            val threadId = task.result?.id
            if (task.isSuccessful && threadId != null) {

                Toast.makeText(this@NewGroupActivity, "Group $groupName created.", Toast.LENGTH_LONG).show()
                deselectAll()
                ChatActivity.openActivity(this@NewGroupActivity, true, ChatActivity.Companion.IntentData(threadId))
            }
        }
    }
}
