package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.OnProgressListener
import com.siddhantkushwaha.raven.NotificationSender
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.MessageAdapter
import com.siddhantkushwaha.raven.commonUtility.*
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.localEntity.RavenMessage
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.localEntity.RavenThread
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.ravenUtility.RavenUtils
import com.yalantis.ucrop.UCrop
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_chat.*
import java.lang.Exception
import kotlin.math.max

class ChatActivity : AppCompatActivity() {

    private val tag = ChatActivity::class.java.toString()

    private var userId: String? = null
    private var threadId: String? = null

    private var firstVisibleItemPosition: IntArray = intArrayOf(-1, -1)
    private var lastVisibleItemPosition: IntArray = intArrayOf(-1, -1)

    private var userManager: UserManager? = null
    private var threadManager: ThreadManager? = null

    private var userEventListener: EventListener<DocumentSnapshot>? = null
    private var threadEventListener: EventListener<QuerySnapshot>? = null

    private var user: User? = null

    private var realm: Realm? = null
    private var results: RealmResults<RavenMessage>? = null
    private var ravenMessageAdapter: MessageAdapter? = null
    private var listener: OrderedRealmCollectionChangeListener<RealmResults<RavenMessage>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

        realm = RealmUtil.getCustomRealmInstance(this@ChatActivity)

        userId = intent.getStringExtra(getString(R.string.key_user_id))
        if (userId == null || userId.equals(FirebaseAuth.getInstance().uid)) {
            finish()
        }

        threadId = RavenUtils.getThreadId(FirebaseAuth.getInstance().uid, userId)
        if (threadId == null) {
            finish()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userProfileLayout.setOnClickListener {
            val intent = Intent(this@ChatActivity, ProfileActivity::class.java)
            intent.putExtra(getString(R.string.key_user_id), userId)
            startActivity(intent)
        }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isEmpty()) {
                Alerts.showToast(this@ChatActivity, "Cannot send an empty message.", 2000)
                return@setOnClickListener
            }
            messageEditText.setText("")
            sendMessage(message)
        }

        sendImageButton.setOnClickListener {
            ImageFileHandling.openImageIntent(this@ChatActivity)
        }

        user = User()
        userManager = UserManager()
        threadManager = ThreadManager()

        userEventListener = EventListener { snapshot, _ ->

            if (snapshot != null && snapshot.exists()) {
                val tempUser = snapshot.toObject(User::class.java)
                user?.cloneObject(tempUser)
            }
            updateProfileLayout()
        }

        threadEventListener = EventListener { querySnapshot, _ ->

            val documentChangeList = querySnapshot!!.documentChanges
            documentChangeList.forEach {

                val document = it.document
                val messageId = document.id
                when (it.type) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        try {
                            val message = document.toObject(Message::class.java)
                            realm?.executeTransaction { realm ->

                                var ravenMessage = realm.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()
                                if (ravenMessage == null) {
                                    ravenMessage = RavenMessage()
                                    ravenMessage.messageId = messageId
                                    ravenMessage.threadId = threadId
                                }
                                ravenMessage.cloneObject(message)
                                realm.insertOrUpdate(ravenMessage)
                            }
                        } catch (e: Exception) {
                            Log.e(tag, e.toString())
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        realm?.executeTransaction { realm ->
                            realm.where(RavenMessage::class.java).equalTo("messageId", messageId).findAll().deleteAllFromRealm()
                        }
                    }
                }
            }
        }

        results = realm?.where(RavenMessage::class.java)?.equalTo("threadId", threadId)?.sort("localTimestamp", Sort.ASCENDING, "timestamp", Sort.ASCENDING)?.findAllAsync()

        val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
        ravenMessageAdapter = MessageAdapter(this@ChatActivity, results, false)

        messageRecyclerView.layoutManager = linearLayoutManager
        messageRecyclerView.adapter = ravenMessageAdapter

        messageRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                setScrollPosition(lastVisibleItemPosition[0] == getLastItemPosition())
            }
        }

        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                firstVisibleItemPosition[0] = firstVisibleItemPosition[1]
                lastVisibleItemPosition[0] = lastVisibleItemPosition[1]

                firstVisibleItemPosition[1] = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                lastVisibleItemPosition[1] = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                updateMessageStatus(firstVisibleItemPosition[1], lastVisibleItemPosition[1])
            }
        })

        ravenMessageAdapter?.setOnClickListener { _, position ->
            Log.i(tag, ravenMessageAdapter?.getItem(position)?.text ?: "null")
        }
        ravenMessageAdapter?.setOnLongClickListener { _, position ->

            val ravenMessage = ravenMessageAdapter?.getItem(position)

            if (ravenMessage?.sentByUserId == FirebaseAuth.getInstance().uid) {
                threadManager?.deleteMessageForEveryone(threadId!!, ravenMessage?.messageId!!) {
                    if (it.isSuccessful)
                        Alerts.showToast(this@ChatActivity, "Deleted.", 2000)
                }
            }
        }

        firstVisibleItemPosition[1] = savedInstanceState?.getInt(getString(R.string.key_first_item_position)) ?: -1
        lastVisibleItemPosition[1] = savedInstanceState?.getInt(getString(R.string.key_last_item_position)) ?: -1
        listener = OrderedRealmCollectionChangeListener { results, _ ->

            ravenMessageAdapter?.notifyDataSetChanged()
            setScrollPosition(lastVisibleItemPosition[1] == getLastItemPosition() - 1)
        }
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this.javaClass.toString(), intent.extras)

        NotificationSender.cancelNotification(this@ChatActivity, threadId, 0)

        realm?.executeTransactionAsync { realmIns ->
            val ravenThread = realmIns.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
            if (ravenThread != null) {
                ravenThread.read = true
                realmIns.insertOrUpdate(ravenThread)
            }
        }

        userManager?.startUserSyncByUserId(this@ChatActivity, userId, userEventListener)
        threadManager?.startThreadSyncByThreadId(this@ChatActivity, threadId, threadEventListener)

        results?.addChangeListener(listener!!)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putInt(getString(R.string.key_first_item_position), firstVisibleItemPosition[1])

        outState?.putInt(getString(R.string.key_last_item_position), lastVisibleItemPosition[1])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ImageFileHandling.pickImage -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(tag, data?.data?.toString())
                    ImageFileHandling.startCrop(this@ChatActivity, data?.data ?: return)
                }
            }

            UCrop.REQUEST_CROP -> {
                handleCropResult(UCrop.getOutput(data ?: return) ?: return)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)

        results?.removeAllChangeListeners()
    }

    private fun updateProfileLayout() {

        nameTextView.text = user?.userProfile?.name ?: user?.phoneNumber ?: getString(R.string.key_user_id)
        GlideUtils.loadProfilePhotoCircle(this, imageRelativeLayout, user?.userProfile?.picUrl)
    }

    private fun setScrollPosition(scrollToLast: Boolean?) {

        when {
            lastVisibleItemPosition[1] == -1 || lastVisibleItemPosition[1] == getLastItemPosition() -> {
                messageRecyclerView.scrollToPosition(getLastItemPosition())
            }

            scrollToLast ?: false -> {
                messageRecyclerView.smoothScrollToPosition(getLastItemPosition())
            }

            firstVisibleItemPosition[1] > -1 -> {
                messageRecyclerView.scrollToPosition(firstVisibleItemPosition[1])
            }
        }
    }

    private fun getLastItemPosition(): Int {
        val pos: Int = (ravenMessageAdapter?.itemCount ?: 0) - 1
        return max(0, pos)
    }

    private fun sendMessage(message: String) {

        val encryptedMessage = ThreadManager.encryptMessage(threadId, message) ?: return

        val messageObject = Message(encryptedMessage, Timestamp.now(), FirebaseAuth.getInstance().uid!!, userId!!)
        threadManager?.sendMessage(threadId!!, messageObject)
    }

    private fun updateMessageStatus(low: Int, high: Int) {
        for (i in low..high) {
            try {
                val ravenMessage = ravenMessageAdapter?.getItem(i) ?: continue
                if (ravenMessage.seenAt == null && ravenMessage.sentByUserId != FirebaseAuth.getInstance().uid) {
                    threadManager?.markMessageAsRead(threadId!!, ravenMessage.messageId, Timestamp.now(), null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleCropResult(uri: Uri) {

        val messageObject = Message(null, Timestamp.now(), FirebaseAuth.getInstance().uid!!, userId!!)
        threadManager?.sendMessage(threadId!!, messageObject, uri) {

            val progress:Double = it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()
            Log.i(tag, progress.toString())
        }
    }
}