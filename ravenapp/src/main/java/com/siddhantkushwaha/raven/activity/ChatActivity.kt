package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.JsonObject
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ImageUtil
import com.siddhantkushwaha.raven.NotificationSender
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.MessageAdapter
import com.siddhantkushwaha.raven.common.utility.GsonUtils
import com.siddhantkushwaha.raven.utility.RealmUtil
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.localEntity.RavenMessage
import com.siddhantkushwaha.raven.localEntity.RavenThread
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil
import com.siddhantkushwaha.raven.utility.GlideUtils
import com.siddhantkushwaha.raven.utility.RavenUtils
import com.siddhantkushwaha.raven.utility.UCropUtil
import com.yalantis.ucrop.UCrop
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_chat.*
import kotlin.math.max

class ChatActivity : AppCompatActivity() {

    companion object {
        // data class IntentData(val userId: String, val threadId: String)
        data class IntentData(val threadId: String)

        @JvmStatic
        fun getIntent(context: Context, intentData: IntentData): Intent {

            val intent = Intent(context, ChatActivity::class.java)
            // intent.putExtra("userId", intentData.userId)
            intent.putExtra("threadId", intentData.threadId)

            return intent
        }

        @JvmStatic
        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = getIntent(activity, intentData)
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

    private var threadDocEventListener: EventListener<DocumentSnapshot>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

        val intentData = getIntentData(this)
        realm = RealmUtil.getCustomRealmInstance(this)

        threadId = intentData.threadId
        userId = RavenUtils.getUserId(threadId!!, FirebaseAuth.getInstance().uid!!)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userProfileLayout.setOnClickListener {
            ProfileActivity.openActivity(this@ChatActivity, false, ProfileActivity.Companion.IntentData(userId!!))
        }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this@ChatActivity, "Cannot send an empty message.", 2000).show()
                return@setOnClickListener
            }
            messageEditText.setText("")
            sendMessage(message)
        }

        sendImageButton.setOnClickListener {
            ImageUtil.openImageIntent(this@ChatActivity)
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

        threadDocEventListener = EventListener { doc, _ ->

            if (doc != null && doc.exists()) {
                try {
                    val docData = doc.data?.get("backgroundMetadata")
                    var fileRef: String? = null
                    var alpha: Float? = null
                    if (docData != null) {
                        val backgroundMetadata = GsonUtils.fromGson(GsonUtils.toGson(docData), JsonObject::class.java)
                        fileRef = backgroundMetadata.getAsJsonPrimitive("fileRef").asString
                        alpha = backgroundMetadata.getAsJsonPrimitive("opacity").asFloat
                    }

                    if (fileRef != null)
                        FirebaseStorageUtil().getDownloadUrl(this@ChatActivity, fileRef) {
                            // this transaction is not async because of loadBackground() after this
                            updateBackground(it, alpha)
                        }
                    else
                        updateBackground(null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
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

            val ravenMessage = ravenMessageAdapter?.getItem(position)
            val fileRef = ravenMessage?.fileRef ?: return@setOnClickListener
            ImageFullScreenActivity.openActivity(this@ChatActivity, false, ImageFullScreenActivity.Companion.IntentData(fileRef))
        }
        ravenMessageAdapter?.setOnLongClickListener { _, position ->

            val ravenMessage = ravenMessageAdapter?.getItem(position)
                    ?: return@setOnLongClickListener
            if (ravenMessage.sentByUserId == FirebaseAuth.getInstance().uid) {
                threadManager?.deleteMessageForEveryone(threadId!!, ravenMessage.messageId, ravenMessage.fileRef) {
                    if (it.isSuccessful)
                        Toast.makeText(this@ChatActivity, "Deleted.", 2000).show()
                }
            }
        }

        firstVisibleItemPosition[1] = savedInstanceState?.getInt(getString(R.string.key_first_item_position))
                ?: -1
        lastVisibleItemPosition[1] = savedInstanceState?.getInt(getString(R.string.key_last_item_position))
                ?: -1
        listener = OrderedRealmCollectionChangeListener { _, _ ->

            ravenMessageAdapter?.notifyDataSetChanged()
            setScrollPosition(lastVisibleItemPosition[1] == getLastItemPosition() - 1)
        }
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this.javaClass.toString(), intent.extras)

        NotificationSender.cancelNotification(this@ChatActivity, threadId, 0)

        markThreadRead()
        loadBackGround()

        userManager?.startUserSyncByUserId(this@ChatActivity, userId, userEventListener)
        threadManager?.startThreadSyncByThreadId(this@ChatActivity, threadId, threadEventListener)
        threadManager?.startThreadDocSyncByThreadId(this@ChatActivity, threadId, threadDocEventListener)

        results?.addChangeListener(listener!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(getString(R.string.key_first_item_position), firstVisibleItemPosition[1])

        outState.putInt(getString(R.string.key_last_item_position), lastVisibleItemPosition[1])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ImageUtil.pickImage -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(tag, data?.data?.toString())
                    UCropUtil.startCrop(this@ChatActivity, data?.data ?: return)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_change_background -> {
                ChatBackgroundGallery.openActivity(this@ChatActivity, false,
                        ChatBackgroundGallery.Companion.IntentData(FirebaseAuth.getInstance().uid!!, threadId!!))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //TODO -- make this better, this is just a quick fix
    override fun onBackPressed() {
        super.onBackPressed()

        onSupportNavigateUp()
    }

    private fun updateProfileLayout() {

        nameTextView.text = user?.userProfile?.name ?: user?.phoneNumber
                ?: getString(R.string.default_name)
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

            val progress: Double = it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()
            Log.i(tag, progress.toString())
        }
    }

    private fun markThreadRead() {

        realm?.executeTransactionAsync { realmIns ->
            val ravenThread = realmIns.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
            if (ravenThread != null) {
                ravenThread.read = true
                realmIns.insertOrUpdate(ravenThread)
            }
        }
    }

    private fun updateBackground(fileUrl: String?, alpha: Float?) {

        realm?.executeTransactionAsync { realmIns ->
            val ravenThread = realmIns.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
            if (ravenThread != null) {
                ravenThread.backgroundFileUrl = fileUrl
                ravenThread.backgroundOpacity = alpha
                realmIns.insertOrUpdate(ravenThread)
            }

            runOnUiThread {
                loadBackGround()
            }
        }
    }

    private fun loadBackGround() {

        realm?.executeTransactionAsync { realmIns ->
            val ravenThread = realmIns.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()

            val uri = ravenThread?.backgroundFileUrl
            val opacity = ravenThread?.backgroundOpacity ?: 1F
            runOnUiThread {
                loadChatBackground(uri, opacity)
            }
        }
    }

    private fun loadChatBackground(uri: String?, alpha: Float?) {

        background.alpha = alpha ?: 1F

        val requestOptions = RequestOptions()
        requestOptions.error(R.drawable.artwork_raven)
        requestOptions.placeholder(R.drawable.artwork_raven)

        GlideUtils.loadChatBackground(this@ChatActivity, uri, requestOptions, background)
    }
}