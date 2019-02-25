package com.siddhantkushwaha.raven.activity.main

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.raven.BuildConfig
import com.siddhantkushwaha.raven.HomeActivityUtil
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.AboutActivity
import com.siddhantkushwaha.raven.activity.ContactsActivity
import com.siddhantkushwaha.raven.adapter.ThreadAdapter
import com.siddhantkushwaha.raven.entity.Thread
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.realm.utility.RavenMessageUtil
import com.siddhantkushwaha.raven.realm.utility.RavenThreadUtil
import com.siddhantkushwaha.raven.realm.utility.RavenUserUtil
import com.siddhantkushwaha.raven.utility.Common
import com.siddhantkushwaha.raven.utility.FirebaseUtils
import com.siddhantkushwaha.raven.utility.GlideUtilV2
import com.siddhantkushwaha.raven.utility.RealmUtil
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_toolbar.*


class HomeActivity : AppCompatActivity() {

    companion object {
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, HomeActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    private val tag = HomeActivity::class.java.toString()

    private var drawerToggle: ActionBarDrawerToggle? = null

    private val user: User = User()
    private val userManager: UserManager = UserManager()
    private val threadManager: ThreadManager = ThreadManager()

    private lateinit var currentUserEventListener: EventListener<DocumentSnapshot>
    private lateinit var allThreadsFirestoreListener: EventListener<QuerySnapshot>

    private val allThreadDocIds = HashMap<String, DocumentSnapshot>()

    private lateinit var realm: Realm
    private lateinit var allThreads: RealmResults<RavenThread>
    private lateinit var allThreadsAdapter: ThreadAdapter
    private lateinit var allThreadsRealmListener: OrderedRealmCollectionChangeListener<RealmResults<RavenThread>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().currentUser?.phoneNumber!!)
        Crashlytics.setUserName(FirebaseAuth.getInstance().uid)

        setContentView(R.layout.activity_home)

        realm = RealmUtil.getCustomRealmInstance(this@HomeActivity)

        setSupportActionBar(toolbar)
        toolbar.title = "Raven"
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

                R.id.action_clear_data -> {
                    Common.clearData(this@HomeActivity)
                }

                R.id.action_about -> {
                    actionAbout()
                }
            }
            false
        }

        navigation.menu.findItem(R.id.action_clear_data).isVisible = BuildConfig.DEBUG

        contacts.setOnClickListener {
            ContactsActivity.openActivity(this@HomeActivity, false)

            // Do not remove the below piece of code, comment it instead.

            /*
            val threadId = "OIVpvvIjA6fJFbkL4mrlX95zTke2S2HeXgDdtPgwQFk6gRtzyg6dAol1"
            val requestCode = Random().nextInt(100000)
            val intent = ChatActivity.getIntent(this@HomeActivity, ChatActivity.Companion.IntentData(threadId))
            val notificationSender = NotificationSender(this@HomeActivity,
                    threadId,
                    requestCode,
                    "admin",
                    "notificaiton pending intent test..",
                    intent)

            notificationSender.sendNotificationWithReplyAction("12345", threadId, user?.userProfile?.picUrl)
            */
        }

        allThreadsFirestoreListener = EventListener { t, firebaseFirestoreException ->

            firebaseFirestoreException?.printStackTrace()

            t?.documentChanges?.forEach {

                val threadSnap: DocumentSnapshot = it.document
                val threadId = threadSnap.id

                when (it.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {

                        RavenThreadUtil.setThread(realm, false, threadId, FirebaseAuth.getInstance().uid!!, threadSnap)

                        val thread = threadSnap.toObject(Thread::class.java)
                        val users: ArrayList<String>? = thread?.users

                        if (thread?.groupDetails == null && users != null && users.size == 2) {
                            var anotherUserId = users[0]
                            if (users[0] == FirebaseAuth.getInstance().uid)
                                anotherUserId = users[1]
                            userManager.startUserSyncByUserId(this@HomeActivity, anotherUserId) { documentSnapshot, firebaseFirestoreException ->
                                RavenUserUtil.setUser(realm, false, anotherUserId, documentSnapshot, firebaseFirestoreException)
                            }
                        }

                        val lm = RavenThreadUtil.findMostRecentMessage(FirebaseAuth.getInstance().uid!!, thread?.messages)
                        if (lm != null) {
                            RavenMessageUtil.setMessage(realm, false, threadId, lm.key, lm.value, null)
                            RavenThreadUtil.setLastMessage(realm, false, threadId, lm.key)
                        } else {
                            RavenThreadUtil.setLastMessage(realm, false, threadId)
                        }

                        allThreadDocIds[threadId] = threadSnap
                    }

                    DocumentChange.Type.REMOVED -> {
                        RavenThreadUtil.setThread(realm, false, threadId, FirebaseAuth.getInstance().uid!!)
                        allThreadDocIds.remove(threadId)
                    }
                }
            }

            if (::allThreads.isInitialized)
                for (rt in allThreads) {
                    if (!allThreadDocIds.containsKey(rt.threadId))
                        RavenThreadUtil.setThread(realm, false, rt.threadId, FirebaseAuth.getInstance().uid!!)
                }
        }

        allThreads = realm.where(RavenThread::class.java).equalTo("userId", FirebaseAuth.getInstance().uid).sort("timestamp", Sort.DESCENDING, "lastMessage.timestamp", Sort.DESCENDING).findAllAsync()

        allThreadsAdapter = ThreadAdapter(this@HomeActivity, allThreads)
        threadListView.emptyView = emptyView
        threadListView.adapter = allThreadsAdapter

        allThreadsRealmListener = OrderedRealmCollectionChangeListener { res, _ ->
            allThreadsAdapter.notifyDataSetChanged()
        }

        threadListView.setOnItemClickListener { _, _, position, _ ->

            ChatActivity.openActivity(this@HomeActivity, false,
                    ChatActivity.Companion.IntentData(allThreadsAdapter.getItem(position)?.threadId!!))
        }

        threadListView.setOnItemLongClickListener { parent, view, position, id ->

            // TODO only for those with the debug version
            if (BuildConfig.DEBUG) {

                val threadId = allThreadsAdapter.getItem(position)?.threadId
                        ?: return@setOnItemLongClickListener true

                val doc = FirebaseUtils.getFirestoreDb(true).collection("threads").document(threadId)
                doc.delete()
            }

            true
        }

        currentUserEventListener = EventListener { snapshot, _ ->

            if (snapshot != null && snapshot.exists()) {
                user.cloneObject(snapshot.toObject(User::class.java)!!)
            }
            updateProfileUi()
        }

        // RavenContactSync.setupSync(this@HomeActivity)

        val map = HashMap<String, Any>()
        map[UserManager.KEY_PHONE] = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
        userManager.setUserFields(FirebaseAuth.getInstance().uid!!, map) {
            it.exception?.printStackTrace()
        }

        HomeActivityUtil.setUserMetaData(userManager, FirebaseAuth.getInstance().uid!!)

        FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().uid!!)
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        userManager.startUserSyncByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid!!, currentUserEventListener)
        threadManager.startAllThreadsSyncByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid, allThreadsFirestoreListener)

        allThreads.addChangeListener(allThreadsRealmListener)
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)

        allThreads.removeAllChangeListeners()
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return drawerToggle?.onOptionsItemSelected(item) ?: false || super.onOptionsItemSelected(item)
    }

    private fun actionMyProfile() {
        MyProfileActivity.openActivity(this@HomeActivity, false)
    }

    private fun actionLogout() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().uid!!)

        FirebaseAuth.getInstance().signOut()
        LoginActivity.openActivity(this@HomeActivity, true)
    }

    private fun actionAbout() {
        AboutActivity.openActivity(this@HomeActivity, false)
    }

    private fun updateProfileUi() {

        navigation.getHeaderView(0).findViewById<TextView>(R.id.nameTextView).text = user.userProfile?.name
                ?: getString(R.string.default_name)
        navigation.getHeaderView(0).findViewById<TextView>(R.id.phoneTextView).text = user.phoneNumber
                ?: "Phone"
        GlideUtilV2.loadProfilePhotoCircle(this@HomeActivity, navigation.getHeaderView(0).findViewById(R.id.displayPicImageView), user.userProfile?.picUrl)
    }
}