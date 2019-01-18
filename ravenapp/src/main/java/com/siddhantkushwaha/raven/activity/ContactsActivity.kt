package com.siddhantkushwaha.raven.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.commonUtility.ActivityInfo
import com.siddhantkushwaha.raven.commonUtility.Alerts
import com.siddhantkushwaha.raven.commonUtility.ContactsUtil
import com.siddhantkushwaha.raven.commonUtility.RealmUtil
import com.siddhantkushwaha.raven.localEntity.RavenUser
import com.siddhantkushwaha.raven.syncAdapter.SyncAdapter
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity() {

    private val tag = ContactsActivity::class.java.toString()

    private var realm: Realm? = null
    private var results: RealmResults<RavenUser>? = null
    private var ravenContactAdapter: ContactAdapter? = null
    private var listener: OrderedRealmCollectionChangeListener<RealmResults<RavenUser>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_contacts)

        realm = RealmUtil.getCustomRealmInstance(this@ContactsActivity)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        refreshButton.setOnClickListener {
            Alerts.showToast(this@ContactsActivity, "Syncing contacts.", 2000)
//            RavenContactSync.reSync(this@ContactsActivity)
//            AppInfo.openAppInfo(this);
            ContactsUtil.getReadContactsPermission(this@ContactsActivity)
            SyncAdapter.syncContacts(this@ContactsActivity)
        }

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
            setDefaultResults()
            false
        }

        userListView.emptyView = emptyView
        retrieveRavenContacts()
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        results?.addChangeListener(listener!!)
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)

        results?.removeAllChangeListeners()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun filter(query: String) {

        Log.i(tag, "Searching for $query")
        val searchResults = realm?.where(RavenUser::class.java)?.equalTo("inContacts", true)?.like("contactName", "*$query*")?.sort("contactName", Sort.ASCENDING)?.findAll()
        Log.i(tag, "Searching for ${searchResults?.size}")
        val searchAdapter = ContactAdapter(this@ContactsActivity, searchResults)

        userListView.adapter = searchAdapter
        userListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@ContactsActivity, ChatActivity::class.java)
            intent.putExtra(getString(R.string.key_user_id), searchAdapter.getItem(position)!!.userId)
            startActivity(intent)
        }
    }

    private fun retrieveRavenContacts() {

        results = realm?.where(RavenUser::class.java)?.equalTo("inContacts", true)?.sort("contactName", Sort.ASCENDING)?.findAllAsync()
        ravenContactAdapter = ContactAdapter(this@ContactsActivity, results)
        listener = OrderedRealmCollectionChangeListener { _, _ -> ravenContactAdapter?.notifyDataSetChanged() }

        setDefaultResults()
    }

    private fun setDefaultResults() {
        userListView.adapter = ravenContactAdapter
        userListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@ContactsActivity, ChatActivity::class.java)
            intent.putExtra(getString(R.string.key_user_id), ravenContactAdapter?.getItem(position)!!.userId)
            startActivity(intent)
        }
    }
}