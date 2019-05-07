package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.GroupUserAdapter
import com.siddhantkushwaha.raven.entity.Thread
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.realm.utility.RavenThreadUtil
import com.siddhantkushwaha.raven.realm.utility.RavenUserUtil
import com.siddhantkushwaha.raven.utility.GlideUtil
import com.siddhantkushwaha.raven.utility.RealmUtil
import com.siddhantkushwaha.raven.utility.UiUtil
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_group_profile.*
import kotlinx.android.synthetic.main.layout_app_bar_thread_profile.*

class GroupProfileActivity : AppCompatActivity() {

    companion object {
        data class IntentData(val threadId: String)

        @JvmStatic
        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = Intent(activity, GroupProfileActivity::class.java)
            intent.putExtra("threadId", intentData.threadId)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }

        @JvmStatic
        fun getIntentData(activity: Activity): IntentData {

            val intent = activity.intent
            return IntentData(intent.getStringExtra("threadId"))
        }
    }

    private lateinit var intentData: IntentData

    private val userManager = UserManager()
    private val threadManager = ThreadManager()

    private lateinit var threadDocEventListener: EventListener<DocumentSnapshot>

    private lateinit var realm: Realm
    private lateinit var ravenThreadResult: RealmResults<RavenThread>
    private lateinit var ravenThreadChangeListener: OrderedRealmCollectionChangeListener<RealmResults<RavenThread>>

    private lateinit var adapter: GroupUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_profile)

        intentData = getIntentData(this@GroupProfileActivity)

        realm = RealmUtil.getCustomRealmInstance(this@GroupProfileActivity)

        threadDocEventListener = EventListener { threadSnap, firebaseFirestoreException1 ->

            firebaseFirestoreException1?.printStackTrace()

            RavenThreadUtil.setThread(realm, true, intentData.threadId, FirebaseAuth.getInstance().uid!!, threadSnap)

            val thread = threadSnap?.toObject(Thread::class.java)

            thread?.users?.forEach { userId ->
                userManager.startUserSyncByUserId(this@GroupProfileActivity, userId) { userSnap, firebaseFirestoreException2 ->
                    RavenUserUtil.setUser(realm, false, userId, userSnap, firebaseFirestoreException2)
                }
            }
        }

        ravenThreadResult = realm.where(RavenThread::class.java).equalTo("threadId", intentData.threadId).findAllAsync()
        ravenThreadChangeListener = OrderedRealmCollectionChangeListener { _, _ ->

            updateProfileLayout()

            val rt = ravenThreadResult.first(null)
            val users = rt?.users

            if (!::adapter.isInitialized && rt != null && users != null) {
                adapter = GroupUserAdapter(this@GroupProfileActivity, users, rt)
                userListView.adapter = adapter
            }

            (userListView.adapter as? GroupUserAdapter)?.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()

        threadManager.startThreadSyncByThreadId(this@GroupProfileActivity, intentData.threadId, threadDocEventListener)

        ravenThreadResult.addChangeListener(ravenThreadChangeListener)
    }

    override fun onStop() {
        super.onStop()

        ravenThreadResult.removeAllChangeListeners()
    }

    private fun updateProfileLayout() {

        val rt = ravenThreadResult.first(null)

        collapsingToolbar.title = rt?.groupName ?: "Group"

        GlideUtil.loadProfilePhotoSquare(this@GroupProfileActivity, imageRelativeLayout, rt?.picUrl)
        GlideUtil.loadImageAsBitmap(this@GroupProfileActivity, rt?.picUrl, RequestOptions(), object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                Palette.from(resource).generate {
                    val swatch = it?.darkMutedSwatch
                    if (swatch != null) {
                        collapsingToolbar.setContentScrimColor(swatch.rgb)
                        collapsingToolbar.setBackgroundColor(swatch.rgb)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            UiUtil.setStatusBarColor(this@GroupProfileActivity, UiUtil.manipulateColor(swatch.rgb, 0.8f), UiUtil.DARK)
                        }
                    }
                }
            }
        })

        members.text = "${rt?.users?.size ?: 0} members"
    }
}
