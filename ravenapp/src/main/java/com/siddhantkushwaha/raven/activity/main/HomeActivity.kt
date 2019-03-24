package com.siddhantkushwaha.raven.activity.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
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
import com.siddhantkushwaha.raven.custom.NavigationIconClickListener
import com.siddhantkushwaha.raven.entity.Thread
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.realm.utility.RavenMessageUtil
import com.siddhantkushwaha.raven.realm.utility.RavenThreadUtil
import com.siddhantkushwaha.raven.realm.utility.RavenUserUtil
import com.siddhantkushwaha.raven.utility.FirebaseUtils
import com.siddhantkushwaha.raven.utility.RealmUtil
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_home.*

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

    private lateinit var navigationIconClickListener: NavigationIconClickListener

    private val userManager: UserManager = UserManager()
    private val threadManager: ThreadManager = ThreadManager()

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

        setActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)

        navigationIconClickListener = NavigationIconClickListener(
                this@HomeActivity,
                linearLayout2,
                AccelerateInterpolator(),
                getDrawable(R.drawable.ham_menu_white),
                getDrawable(R.drawable.btn_close_white)
        )

        toolbar.setNavigationOnClickListener(navigationIconClickListener)

        btn_my_profile.setOnClickListener {
            MyProfileActivity.openActivity(this@HomeActivity, false)
        }

        btn_about.setOnClickListener {
            AboutActivity.openActivity(this@HomeActivity, false)
        }

        btn_logout.setOnClickListener {
            logout()
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

                        val lastSyncedMessage = RavenThreadUtil.getLastMessageByUserId(FirebaseAuth.getInstance().uid!!, thread?.messages)
                        if (lastSyncedMessage != null) {
                            RavenMessageUtil.setMessage(realm, false, threadId, lastSyncedMessage.key, lastSyncedMessage.value)
                        }

                        RavenThreadUtil.setLastMessage(realm, false, threadId, FirebaseAuth.getInstance().uid!!)
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

        allThreads = realm.where(RavenThread::class.java).equalTo("userId", FirebaseAuth.getInstance().uid).sort("timestamp", Sort.DESCENDING).findAllAsync()

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

        threadManager.startAllThreadsSyncByUserId(this@HomeActivity, FirebaseAuth.getInstance().uid, allThreadsFirestoreListener)

        allThreads.addChangeListener(allThreadsRealmListener)
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)

        allThreads.removeAllChangeListeners()
    }

    private fun logout() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().uid!!)

        FirebaseAuth.getInstance().signOut()
        LoginActivity.openActivity(this@HomeActivity, true)
    }
}
