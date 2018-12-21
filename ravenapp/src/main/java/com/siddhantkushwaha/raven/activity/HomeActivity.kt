package com.siddhantkushwaha.raven.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.RavenContactSync
import com.siddhantkushwaha.raven.adapter.ThreadAdapter
import com.siddhantkushwaha.raven.commonUtility.ActivityInfo
import com.siddhantkushwaha.raven.commonUtility.GlideUtils
import com.siddhantkushwaha.raven.commonUtility.RealmUtil
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.ThreadIndex
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.localEntity.RavenMessage
import com.siddhantkushwaha.raven.localEntity.RavenThread
import com.siddhantkushwaha.raven.localEntity.RavenUser
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.Exception
import java.util.HashMap


class HomeActivity : AppCompatActivity() {

    private val tag = HomeActivity::class.java.toString()

    private var drawerToggle: ActionBarDrawerToggle? = null

    private var user: User? = null
    private var userManager: UserManager? = null
    private var currentUserEventListener: EventListener<DocumentSnapshot>? = null

    private var threadManager: ThreadManager? = null
    private var threadIndexEventListener: EventListener<QuerySnapshot>? = null

    private var realm: Realm? = null
    private var results: RealmResults<RavenThread>? = null
    private var ravenThreadAdapter: ThreadAdapter? = null
    private var listener: OrderedRealmCollectionChangeListener<RealmResults<RavenThread>>? = null

    private var userThreadHashMap: HashMap<String, String>? = null
    private var threadUserEventListener: EventListener<DocumentSnapshot>? = null

    private var lastMessageThreadHashMap: HashMap<String, String>? = null
    private var lastMessageEventListener: EventListener<DocumentSnapshot>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().currentUser?.phoneNumber!!)
        Crashlytics.setUserName(FirebaseAuth.getInstance().uid)

        setContentView(R.layout.activity_home)

        realm = RealmUtil.getCustomRealmInstance(this@HomeActivity)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        drawerToggle = ActionBarDrawerToggle(this@HomeActivity, drawer_layout, R.string.drawer_open, R.string.drawer_close)
        drawer_layout.addDrawerListener(drawerToggle!!)
        navigation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_my_profile -> {
                    actionMyProfile()
                }

                R.id.action_logout -> {
                    actionLogout()
                }
            }
            false
        }

        contacts.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ContactsActivity::class.java))
        }

        userThreadHashMap = HashMap()
        lastMessageThreadHashMap = HashMap()
        threadIndexEventListener = EventListener { querySnapshot, _ ->

            val documentChangeList = querySnapshot?.documentChanges
            documentChangeList?.forEach {
                when (it.type) {

                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {

                        try {
                            val threadIndex = it.document.toObject(ThreadIndex::class.java)
                            realm?.executeTransaction { realm ->

                                var ravenThread: RavenThread? = realm.where(RavenThread::class.java).equalTo("threadId", threadIndex.threadId).findFirst()

                                if (ravenThread == null) {
                                    ravenThread = RavenThread()
                                    ravenThread.threadId = threadIndex.threadId
                                    ravenThread.userId = FirebaseAuth.getInstance().uid

                                    realm.insert(ravenThread)
                                }
                            }

                            userThreadHashMap!![it.document.id] = threadIndex.threadId
                            userManager!!.startUserSyncByUserId(this@HomeActivity, it.document.id, threadUserEventListener)

                            if (threadIndex.lastMessageId != null) {
                                lastMessageThreadHashMap!![threadIndex.lastMessageId] = threadIndex.threadId
                                threadManager!!.startMessageSyncByMessageId(this@HomeActivity, threadIndex.threadId, threadIndex.lastMessageId, lastMessageEventListener)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    DocumentChange.Type.REMOVED -> {

                        try {
                            val threadIndex = it.document.toObject(ThreadIndex::class.java)
                            realm?.executeTransaction { realm ->
                                realm.where(RavenThread::class.java).equalTo("threadId", threadIndex.threadId).findAll().deleteAllFromRealm()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        threadUserEventListener = EventListener { snapshot, _ ->

            if (snapshot != null) {

                val userId = snapshot.id
                val threadId = userThreadHashMap!![userId]!!
                if (snapshot.exists()) {
                    try {
                        realm?.executeTransaction { realm ->

                            var ravenUser = realm.where(RavenUser::class.java).equalTo("userId", userId).findFirst()
                            if (ravenUser == null) {
                                ravenUser = RavenUser()
                                ravenUser.userId = userId
                            }
                            ravenUser.cloneObject(snapshot.toObject(User::class.java))
                            ravenUser = realm.copyToRealmOrUpdate(ravenUser)

                            val ravenThread = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                            ravenThread!!.user = ravenUser
                            realm.insertOrUpdate(ravenThread)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                } else {
                    realm?.executeTransaction { realm ->
                        val ravenThread = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ravenThread!!.user = null
                        realm.insertOrUpdate(ravenThread)
                    }
                }
            }
        }

        lastMessageEventListener = EventListener { snapshot, _ ->

            if (snapshot != null) {

                val messageId = snapshot.id
                val threadId = lastMessageThreadHashMap!![messageId]!!
                if (snapshot.exists()) {
                    try {
                        realm?.executeTransaction { realm ->
                            var ravenMessage = realm.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()
                            if (ravenMessage == null) {
                                ravenMessage = RavenMessage()
                                ravenMessage.messageId = messageId
                            }
                            ravenMessage.threadId = threadId
                            ravenMessage.cloneObject(snapshot.toObject(Message::class.java))
                            ravenMessage = realm.copyToRealmOrUpdate(ravenMessage)

                            val ravenThread = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                            ravenThread!!.lastMessage = ravenMessage

                            ravenThread.read = ravenMessage.seenAt != null

                            realm.insertOrUpdate(ravenThread)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                } else {
                    realm?.executeTransaction { realm ->
                        val ravenThread = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ravenThread!!.lastMessage = null
                        realm.insertOrUpdate(ravenThread)

                        realm.where(RavenMessage::class.java).equalTo("messageId", messageId).findAll().deleteAllFromRealm()
                    }
                }
            }
        }

        results = realm?.where(RavenThread::class.java)?.equalTo("userId", FirebaseAuth.getInstance().uid)?.sort("lastMessage.localTimestamp", Sort.DESCENDING, "lastMessage.timestamp", Sort.DESCENDING)?.findAllAsync()

        ravenThreadAdapter = ThreadAdapter(this@HomeActivity, results!!)
        threadListView.emptyView = emptyView
        threadListView.adapter = ravenThreadAdapter

        listener = OrderedRealmCollectionChangeListener { _, _ ->
            ravenThreadAdapter!!.notifyDataSetChanged()
        }

        threadListView.setOnItemClickListener { _, _, position, _ ->

            val intent = Intent(this@HomeActivity, ChatActivity::class.java)
            intent.putExtra(getString(R.string.key_user_id), ravenThreadAdapter?.getItem(position)?.user?.userId)
            startActivity(intent)
        }

        currentUserEventListener = EventListener { snapshot, _ ->

            if (snapshot != null && snapshot.exists()) {
                user?.cloneObject(snapshot.toObject(User::class.java))
            }
            updateProfileUi()
        }

        user = User()
        userManager = UserManager()
        threadManager = ThreadManager()

        RavenContactSync.setupSync(this@HomeActivity)

        val map = HashMap<String, Any>()
        map[UserManager.KEY_USER_ID] = FieldValue.delete()
        map[UserManager.KEY_PHONE] = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
        userManager?.setUserFields(FirebaseAuth.getInstance().uid!!, map) {
            if (it.exception != null)
                Log.e(tag, it.exception.toString())
        }

        map.clear()
        map["versionName"] = packageManager.getPackageInfo(packageName, 0).versionName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            map["versionCode"] = packageManager.getPackageInfo(packageName, 0).longVersionCode
        else {
            try {
                map["versionCode"] = packageManager.getPackageInfo(packageName, 0).versionCode
            } catch (e: Exception) {
                //pass
            }
        }
        map["timestamp"] = Timestamp.now()
        userManager?.setUserMetaData(FirebaseAuth.getInstance().uid!!, map) {
            if (it.exception != null)
                Log.e(tag, it.exception.toString())
        }

        FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().uid!!)
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        userManager!!.startUserSyncByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid!!, currentUserEventListener)
        threadManager!!.startSyncThreadIndexByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid!!, threadIndexEventListener)

        results!!.addChangeListener(listener!!)
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)

        results!!.removeAllChangeListeners()
    }

    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawers()
        else
            super.onBackPressed()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return drawerToggle?.onOptionsItemSelected(item) ?: false || super.onOptionsItemSelected(item)
    }

    private fun actionMyProfile() {
        startActivity(Intent(this@HomeActivity, MyProfileActivity::class.java))
    }

    private fun actionLogout() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().uid!!)

        // TODO figure this out later
        // RealmUtil.clearData(this@HomeActivity)

        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateProfileUi() {

        navigation.getHeaderView(0).findViewById<TextView>(R.id.nameTextView).text = user?.userProfile?.name ?: getString(R.string.default_name)
        navigation.getHeaderView(0).findViewById<TextView>(R.id.phoneTextView).text = user?.phoneNumber ?: "Phone"
        GlideUtils.loadProfilePhotoCircle(this@HomeActivity, navigation.getHeaderView(0).findViewById(R.id.displayPicImageView), user?.userProfile?.picUrl)
    }
}