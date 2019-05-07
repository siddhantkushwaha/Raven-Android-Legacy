package com.siddhantkushwaha.raven.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth

import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import com.siddhantkushwaha.raven.syncAdapter.SyncAdapter
import com.siddhantkushwaha.raven.utility.ContactsUtil
import com.siddhantkushwaha.raven.utility.RealmUtil
import io.realm.*
import kotlinx.android.synthetic.main.activity_base_raven_user.*

@SuppressLint("Registered")
open class BaseRavenUserActivity(private val layout: Int) : AppCompatActivity() {

    internal lateinit var realm: Realm
    private lateinit var allContacts: RealmResults<RavenUser>
    internal lateinit var contactsAdapter: ContactAdapter
    internal lateinit var contactsChangeListener: RealmChangeListener<RealmResults<RavenUser>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layout)

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

        userListView.emptyView = emptyView

        allContacts = realm.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).sort("contactName", Sort.ASCENDING).findAllAsync()

        contactsChangeListener = RealmChangeListener {
            contactsAdapter.notifyDataSetChanged()
        }

        contactsAdapter = ContactAdapter(this, allContacts)

        userListView.adapter = contactsAdapter
    }

    override fun onStart() {
        super.onStart()

        allContacts.addChangeListener(contactsChangeListener)
    }

    override fun onStop() {
        super.onStop()

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
                if (!ContactsUtil.contactsReadPermission(this))
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

        ActivityCompat.requestPermissions(this,
                Array(1) { Manifest.permission.READ_CONTACTS },
                0);
    }

    private fun startSyncing() {
        Toast.makeText(this, "Syncing contacts.", Toast.LENGTH_LONG).show()
        SyncAdapter.syncContacts(this)
    }

    private fun filter(query: String) {

        val regex = "*$query*"
        val searchResults = realm.where(RavenUser::class.java).isNotNull("contactName").notEqualTo("userId", FirebaseAuth.getInstance().uid).like("contactName", regex, Case.INSENSITIVE).sort("contactName", Sort.ASCENDING).findAll()
        val searchAdapter = ContactAdapter(this, searchResults)
        userListView.adapter = searchAdapter
    }

    internal fun getSelected(): List<RavenUser> {
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
}