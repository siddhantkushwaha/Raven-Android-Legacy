package com.siddhantkushwaha.raven.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ContactsUtil
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.main.ChatActivity
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import com.siddhantkushwaha.raven.syncAdapter.SyncAdapter
import com.siddhantkushwaha.raven.utility.RavenUtils
import com.siddhantkushwaha.raven.utility.RealmUtil
import io.realm.*
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.layout12346.*

class ContactsActivity : AppCompatActivity() {

    companion object {
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, ContactsActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    private val tag = ContactsActivity::class.java.toString()

    private lateinit var realm: Realm
    private lateinit var allContacts: RealmResults<RavenUser>
    private lateinit var contactsAdapter: ContactAdapter
    private lateinit var contactsChangeListener: RealmChangeListener<RealmResults<RavenUser>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_contacts)

        realm = RealmUtil.getCustomRealmInstance(this)
        deselectAll()


        toolbar.title = "Contacts"
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

        newGroup.setOnClickListener {
            NewGroupActivity.openActivity(this@ContactsActivity, false)
        }

        userListView.emptyView = emptyView

        allContacts = realm.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).sort("contactName", Sort.ASCENDING).findAllAsync()
        contactsChangeListener = RealmChangeListener {
            Log.i(tag, "Data Changed ${allContacts.size}")
            contactsAdapter.notifyDataSetChanged()
        }

        contactsAdapter = ContactAdapter(this@ContactsActivity, allContacts)

        userListView.adapter = contactsAdapter
        userListView.setOnItemClickListener { _, _, position, _ ->

            val userId = (userListView.adapter as ContactAdapter).getItem(position)?.userId
                    ?: return@setOnItemClickListener
            val threadId = RavenUtils.getThreadId(FirebaseAuth.getInstance().uid, userId)
            ChatActivity.openActivity(this@ContactsActivity, false,
                    ChatActivity.Companion.IntentData(threadId))
        }
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        allContacts.addChangeListener(contactsChangeListener)
    }

    override fun onStop() {
        super.onStop()

        ActivityInfo.setActivityInfo(null, null)

        allContacts.removeAllChangeListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val syncItem = menu?.add(Menu.NONE, 1, 1, "Refresh")
        syncItem?.icon = getDrawable(R.drawable.button_refresh)
        syncItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            1 -> {
                if (!ContactsUtil.contactsReadPermission(this@ContactsActivity))
                    getContactsReadPermission()
                else
                    startSyncing()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {

            0 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startSyncing()
                }
                return
            }

            else -> {
                // other request codes
            }
        }
    }

    private fun getContactsReadPermission() {

        ActivityCompat.requestPermissions(this@ContactsActivity,
                Array(1) { Manifest.permission.READ_CONTACTS },
                0);
    }

    private fun startSyncing() {
        Toast.makeText(this@ContactsActivity, "Syncing contacts.", Toast.LENGTH_LONG).show()
        SyncAdapter.syncContacts(this@ContactsActivity)
    }

    private fun filter(query: String) {

        val regex = "*$query*"
        val searchResults = realm.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).like("contactName", regex, Case.INSENSITIVE).sort("contactName", Sort.ASCENDING).findAll()
        val searchAdapter = ContactAdapter(this@ContactsActivity, searchResults)
        userListView.adapter = searchAdapter
    }

    private fun deselectAll() {

        realm.executeTransactionAsync { realmL ->
            realmL.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).findAll().forEach { ru ->
                ru.selected = false
                realmL.insertOrUpdate(ru)
            }
        }
    }
}