package com.siddhantkushwaha.raven.activity

import android.content.ContentResolver
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.SearchView
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.RavenContactSync
import com.siddhantkushwaha.raven.adapter.ContactAdapter
import com.siddhantkushwaha.raven.commonUtility.ActivityInfo
import com.siddhantkushwaha.raven.commonUtility.Alerts
import com.siddhantkushwaha.raven.commonUtility.RealmUtil
import com.siddhantkushwaha.raven.localEntity.RavenUser
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity() {

    private val tag = ContactsActivity::class.java.toString()

    private var realm:Realm? = null
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
            RavenContactSync.reSync(this@ContactsActivity)
        }

        searchView.setIconifiedByDefault(true)
        searchView.maxWidth = android.R.attr.width

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                filter(p0)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filter(p0)
                return false
            }

        })

        searchView.setOnSearchClickListener {

        }

        searchView.setOnCloseListener {
            filter(null)
            false
        }

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

    private fun filter(query: String?) {
    }

    private fun retrieveRavenContacts() {

        results = realm?.where(RavenUser::class.java)?.sort("contactName", Sort.ASCENDING)?.findAllAsync()
        ravenContactAdapter = ContactAdapter(this@ContactsActivity, results)
        userListView.adapter = ravenContactAdapter

        listener = OrderedRealmCollectionChangeListener { _, _ -> ravenContactAdapter?.notifyDataSetChanged() }

        userListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@ContactsActivity, ChatActivity::class.java)
            intent.putExtra(getString(R.string.key_user_id), results!![position]!!.userId)
            startActivity(intent)
        }
    }
}