package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.ThreadAdapter
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.localEntity.RavenMessage
import com.siddhantkushwaha.raven.localEntity.RavenThread
import com.siddhantkushwaha.raven.localEntity.RavenUser
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
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

    private var user: User? = null
    private var userManager: UserManager? = null
    private var currentUserEventListener: EventListener<DocumentSnapshot>? = null

    private var threadManager: ThreadManager? = null
    private var allThreadsListener: EventListener<QuerySnapshot>? = null

    private var realm: Realm? = null
    private var results: RealmResults<RavenThread>? = null
    private var ravenThreadAdapter: ThreadAdapter? = null
    private var listener: OrderedRealmCollectionChangeListener<RealmResults<RavenThread>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().currentUser?.phoneNumber!!)
        Crashlytics.setUserName(FirebaseAuth.getInstance().uid)

        setContentView(R.layout.activity_home)

        realm = RealmUtil.getCustomRealmInstance(this)

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

                R.id.action_about -> {
                    actionAbout()
                }
            }
            false
        }

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

        allThreadsListener = EventListener { t, firebaseFirestoreException ->

            t?.documentChanges?.forEach {

                val threadId = it.document.id
                when (it.type) {

                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {

                        realm?.executeTransactionAsync { realm ->

                            var rt = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                            if (rt == null) {
                                rt = RavenThread()
                                rt.threadId = threadId
                            }
                            rt.userId = FirebaseAuth.getInstance().uid
                            realm.insertOrUpdate(rt)
                        }

                        val users = it.document["users"] as? ArrayList<String> ?: return@forEach

                        // for threadProfile
                        when {
                            users.size == 2 -> {
                                //one-one thread
                                var anotherUserId = users[0]
                                if (users[0] == FirebaseAuth.getInstance().uid)
                                    anotherUserId = users[1]

                                userManager!!.startUserSyncByUserId(this@HomeActivity, anotherUserId, getThreadUserEventListener(threadId))
                            }

                            users.size > 2 -> {
                                //This means it is a group thread
                            }
                        }

//                        // to sync last message
//                        // threadManager!!.startThreadSyncByThreadId(this@HomeActivity, threadId, getThreadEventListener(threadId, realm))
                        threadManager!!.startLastMessageSyncByTimestamp(this@HomeActivity, threadId) { t, firebaseFirestoreException ->

                            realm?.executeTransactionAsync { realm ->

                                val rt = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                                        ?: return@executeTransactionAsync
                                try {
                                    val messageId = t!!.first().id
                                    val message = t.first().toObject(Message::class.java)

                                    var ravenMessage = realm.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()
                                    if (ravenMessage == null) {
                                        ravenMessage = RavenMessage()
                                        ravenMessage.messageId = messageId
                                        ravenMessage.threadId = threadId
                                    }
                                    ravenMessage.cloneObject(message)
                                    realm.copyToRealmOrUpdate(ravenMessage)

                                    rt.lastMessage = ravenMessage
                                } catch (e: Exception) {
                                    Log.e(tag, "No last message for $threadId")

                                    rt.lastMessage = null
                                }
                                realm.insertOrUpdate(rt)
                            }

                            firebaseFirestoreException?.printStackTrace()
                        }
                    }

                    DocumentChange.Type.REMOVED -> {

                        realm?.executeTransactionAsync { realm ->
                            realm.where(RavenThread::class.java).equalTo("threadId", threadId).findAll().deleteAllFromRealm()
                        }
                    }
                }
            }

            firebaseFirestoreException?.printStackTrace()
        }

        results = realm?.where(RavenThread::class.java)?.equalTo("userId", FirebaseAuth.getInstance().uid)?.sort("timestamp", Sort.DESCENDING, "lastMessage.timestamp", Sort.DESCENDING)?.findAllAsync()

        ravenThreadAdapter = ThreadAdapter(this@HomeActivity, results!!)
        threadListView.emptyView = emptyView
        threadListView.adapter = ravenThreadAdapter

        listener = OrderedRealmCollectionChangeListener { _, _ ->
            ravenThreadAdapter!!.notifyDataSetChanged()
        }

        threadListView.setOnItemClickListener { _, _, position, _ ->

            ChatActivity.openActivity(this@HomeActivity, false,
                    ChatActivity.Companion.IntentData(ravenThreadAdapter?.getItem(position)?.threadId!!))
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

//        RavenContactSync.setupSync(this@HomeActivity)

        val map = HashMap<String, Any>()
        map[UserManager.KEY_USER_ID] = FieldValue.delete()
        map[UserManager.KEY_PHONE] = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
        userManager?.setUserFields(FirebaseAuth.getInstance().uid!!, map) {
            it.exception?.printStackTrace()
        }

        map.clear()
        map["versionName"] = packageManager.getPackageInfo(packageName, 0).versionName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            map["versionCode"] = packageManager.getPackageInfo(packageName, 0).longVersionCode
        else {
            try {
                map["versionCode"] = packageManager.getPackageInfo(packageName, 0).versionCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        userManager?.setUserMetaData(FirebaseAuth.getInstance().uid!!, map) {
            it.exception?.printStackTrace()
        }

        FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().uid!!)
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        userManager!!.startUserSyncByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid!!, currentUserEventListener)
        // threadManager!!.startSyncThreadIndexByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid!!, threadIndexEventListener)
        threadManager!!.syncAllThreadsByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid, allThreadsListener)

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

        // TODO figure this out later
        // RealmUtil.clearData(this@HomeActivity)

        FirebaseAuth.getInstance().signOut()
        LoginActivity.openActivity(this@HomeActivity, true)
    }

    private fun actionAbout() {
        AboutActivity.openActivity(this@HomeActivity, false)
    }

    private fun updateProfileUi() {

        navigation.getHeaderView(0).findViewById<TextView>(R.id.nameTextView).text = user?.userProfile?.name
                ?: getString(R.string.default_name)
        navigation.getHeaderView(0).findViewById<TextView>(R.id.phoneTextView).text = user?.phoneNumber
                ?: "Phone"
        GlideUtilV2.loadProfilePhotoCircle(this@HomeActivity, navigation.getHeaderView(0).findViewById(R.id.displayPicImageView), user?.userProfile?.picUrl)
    }

    private fun getThreadUserEventListener(threadId: String): EventListener<DocumentSnapshot> {

        return EventListener { snapshot, firestoreException ->

            realm?.executeTransactionAsync {
                val ravenThread = it.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ?: return@executeTransactionAsync
                var ravenUser: RavenUser? = null
                if (snapshot != null && snapshot.exists()) {
                    val userId = snapshot.id
                    ravenUser = it.where(RavenUser::class.java).equalTo("userId", userId).findFirst()
                    if (ravenUser == null) {
                        ravenUser = RavenUser()
                        ravenUser.userId = userId
                    }
                    ravenUser.cloneObject(snapshot.toObject(User::class.java))
                    ravenUser = it.copyToRealmOrUpdate<RavenUser?>(ravenUser)
                }
                ravenThread.user = ravenUser
                it.insertOrUpdate(ravenThread)
            }

            firestoreException?.printStackTrace()
        }
    }
}