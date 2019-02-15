package com.siddhantkushwaha.raven.activity.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ImageUtil
import com.siddhantkushwaha.raven.NotificationSender
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.ChatBackgroundGallery
import com.siddhantkushwaha.raven.activity.ImageFullScreenActivity
import com.siddhantkushwaha.raven.activity.ProfileActivity
import com.siddhantkushwaha.raven.adapter.MessageAdapter
import com.siddhantkushwaha.raven.entity.Message
import com.siddhantkushwaha.raven.entity.Thread
import com.siddhantkushwaha.raven.localEntity.RavenMessage
import com.siddhantkushwaha.raven.localEntity.RavenThread
import com.siddhantkushwaha.raven.localEntity.RavenUser
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.utility.*
import com.yalantis.ucrop.UCrop
import io.realm.*
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {

    companion object {
        data class IntentData(val threadId: String)

        @JvmStatic
        fun getIntent(context: Context, intentData: IntentData): Intent {

            val intent = Intent(context, ChatActivity::class.java)
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

    private val tag = this::class.java.toString()

    private lateinit var userId: String
    private lateinit var threadId: String

    private val threadManager: ThreadManager = ThreadManager()

    private lateinit var threadEventListener: EventListener<QuerySnapshot>

    private val realm: Realm = RealmUtil.getCustomRealmInstance(this@ChatActivity)
    private lateinit var allMessages: RealmResults<RavenMessage>
    private lateinit var ravenMessageAdapter: MessageAdapter
    private lateinit var allMessagesListener: OrderedRealmCollectionChangeListener<RealmResults<RavenMessage>>
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var selectedMessages: RealmResults<RavenMessage>
    private lateinit var selectedMessagesListener: OrderedRealmCollectionChangeListener<RealmResults<RavenMessage>>

    private var thread: Thread? = null
    private lateinit var threadDocEventListener: EventListener<DocumentSnapshot>

    private lateinit var ravenThread: RavenThread
    private lateinit var ravenThreadChangeListener: RealmChangeListener<RavenThread>

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

        val intentData = getIntentData(this)

        threadId = intentData.threadId

        if (threadId == RavenUtils.INVALID)
            finish()

        userId = RavenUtils.getUserId(threadId, FirebaseAuth.getInstance().uid!!)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userProfileLayout.setOnClickListener {
            if (userId != RavenUtils.GROUP)
                ProfileActivity.openActivity(this@ChatActivity, false, ProfileActivity.Companion.IntentData(userId))
        }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this@ChatActivity, "Cannot send an empty message.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            messageEditText.setText("")
            sendMessage(messageText = message)
        }

        sendImageButton.setOnClickListener {
            ImageUtil.openImageIntent(this@ChatActivity)
        }

        threadEventListener = getThreadEventListener(threadId, realm)

        threadDocEventListener = EventListener { doc, _ ->

            if (doc != null && doc.exists()) {

                val thread = doc.toObject(Thread::class.java)
                this.thread = thread
                realm.executeTransactionAsync { realm ->

                    var rt = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                    if (rt == null) {
                        rt = RavenThread()
                        rt.threadId = threadId
                    }
                    rt.userId = FirebaseAuth.getInstance().uid

                    rt.cloneObject(thread!!)

                    //users to remove
                    for (ru in rt.users) {
                        if (thread.users.findLast { userId -> ru.userId == userId } == null) {
                            rt.users.removeAll {
                                it.userId == ru.userId
                            }
                        }
                    }

                    //users to add
                    thread.users.forEach { userId ->
                        if (rt.users.findLast { ru -> userId == ru.userId } == null) {
                            var ru = realm.where(RavenUser::class.java).equalTo("userId", userId).findFirst()
                            if (ru == null) {
                                ru = RavenUser()
                                ru.userId = userId
                                ru = realm.copyToRealmOrUpdate(ru)
                            }
                            rt.users.add(ru)
                        }
                    }

                    realm.insertOrUpdate(rt)
                }
            }
        }

        ravenThread = realm.where(RavenThread::class.java).equalTo("threadId", threadId).findFirstAsync()
        ravenThreadChangeListener = RealmChangeListener {

            if (it.isValid) {
                loadBackground(it.backgroundFileRef, it.backgroundOpacity)
                when (userId) {

                    RavenUtils.GROUP -> {
                        nameTextView.text = it.groupName ?: "Raven Group"
                        GlideUtilV2.loadProfilePhotoCircle(this, imageRelativeLayout, it.picUrl)
                    }

                    else -> {
                        nameTextView.text = it.user.contactName ?: it.user.displayName
                                ?: it.user.phoneNumber!!
                        GlideUtilV2.loadProfilePhotoCircle(this, imageRelativeLayout, it.user.picUrl)
                    }
                }
            }
        }

        val messageQuery = realm.where(RavenMessage::class.java).equalTo("threadId", threadId).notEqualTo("deletedBy", FirebaseAuth.getInstance().uid)
        allMessages = messageQuery.sort("localTimestamp", Sort.ASCENDING, "timestamp", Sort.ASCENDING).findAllAsync()
        allMessagesListener = OrderedRealmCollectionChangeListener { res, _ ->

            ravenMessageAdapter.notifyDataSetChanged()

            try {
                val lastMessage = res.last()
                if (lastMessage != null) {
                    // when this user sends a new message, scroll to the bottom
                    if (lastMessage.timestamp == null && lastMessage.sentByUserId == FirebaseAuth.getInstance().uid) {
                        linearLayoutManager.scrollToPosition(ravenMessageAdapter.itemCount - 1)
                    }
                    // if this user receives a new message
                    else if (lastMessage.sentByUserId != FirebaseAuth.getInstance().uid && lastMessage.getSeenByUserId(FirebaseAuth.getInstance().uid!!) == null) {

                        // scroll to bottom only if user is already at the bottom
                        if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == ravenMessageAdapter.itemCount - 2)
                            linearLayoutManager.scrollToPosition(ravenMessageAdapter.itemCount - 1)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "No last message found $threadId")
            }
        }

        selectedMessages = messageQuery.equalTo("selected", true).findAllAsync()

        val actionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.chat_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

                var returnValue = false
                when (item.itemId) {
                    R.id.action_delete -> {

                        val m = selectedMessages.findLast {
                            it.sentByUserId != FirebaseAuth.getInstance().uid
                        }

                        showDeleteDialog(m == null)
                        returnValue = true
                    }

                    R.id.action_select_all -> {
                        setMessageSelectedPropertyForAll(true)
                        returnValue = true
                    }
                }

                return returnValue
            }

            override fun onDestroyActionMode(mode: ActionMode) {

                setMessageSelectedPropertyForAll(false)

                actionMode = null
            }
        }

        selectedMessagesListener = OrderedRealmCollectionChangeListener { _, _ ->
            Log.i(tag, selectedMessages.size.toString())

            if (selectedMessages.size > 0) {

                if (actionMode == null) actionMode = startSupportActionMode(actionModeCallback)
                actionMode?.title = selectedMessages.size.toString()
            } else
                actionMode?.finish()
        }

        ravenMessageAdapter = MessageAdapter(this@ChatActivity, allMessages, false)

        linearLayoutManager = LinearLayoutManager(this@ChatActivity)
        linearLayoutManager.stackFromEnd = true

        messageRecyclerView.layoutManager = linearLayoutManager
        messageRecyclerView.adapter = ravenMessageAdapter
        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                if (firstVisibleItemPosition == -1 || lastVisibleItemPosition == -1)
                    return

                for (i in firstVisibleItemPosition..lastVisibleItemPosition) {

                    val ravenMessage = ravenMessageAdapter.getItem(i)!!
                    if (ravenMessage.sentByUserId != FirebaseAuth.getInstance().uid && ravenMessage.getSeenByUserId(FirebaseAuth.getInstance().uid!!) == null) {
                        threadManager.markMessageAsRead(threadId, ravenMessage.messageId, Timestamp.now(), null)
                    }
                }
            }
        })

        ravenMessageAdapter.setOnClickListener { _, position ->

            val ravenMessage = ravenMessageAdapter.getItem(position)

            if (selectedMessages.size > 0) {

                val messageId = ravenMessageAdapter.getItem(position)?.messageId
                        ?: return@setOnClickListener
                setMessageSelectedProperty(messageId, true)
            } else if (ravenMessage?.fileRef != null) {

                ImageFullScreenActivity.openActivity(this@ChatActivity, false, ImageFullScreenActivity.Companion.IntentData(ravenMessage.fileRef))
            }
        }
        ravenMessageAdapter.setOnLongClickListener { _, position ->

            val messageId = ravenMessageAdapter.getItem(position)?.messageId
                    ?: return@setOnLongClickListener
            setMessageSelectedProperty(messageId, true)
        }
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this.javaClass.toString(), intent.extras)

        NotificationSender.cancelNotification(this@ChatActivity, threadId, 0)

        threadManager.startThreadSyncByThreadId(this@ChatActivity, threadId, threadEventListener)
        threadManager.startThreadDocSyncByThreadId(this@ChatActivity, threadId, threadDocEventListener)

        allMessages.addChangeListener(allMessagesListener)
        selectedMessages.addChangeListener(selectedMessagesListener)
        ravenThread.addChangeListener(ravenThreadChangeListener)
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

        allMessages.removeAllChangeListeners()
        selectedMessages.removeAllChangeListeners()
        ravenThread.removeAllChangeListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_change_background -> {
                ChatBackgroundGallery.openActivity(this@ChatActivity, false,
                        ChatBackgroundGallery.Companion.IntentData(FirebaseAuth.getInstance().uid!!, threadId))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        if (selectedMessages.size > 0) {
            setMessageSelectedPropertyForAll(false)
            return
        }

        super.onBackPressed()
        onSupportNavigateUp()
    }

    private fun getThreadEventListener(threadId: String, realm: Realm?): EventListener<QuerySnapshot> {

        return EventListener { querySnapshot, _ ->

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
                            e.printStackTrace()
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        realm?.executeTransaction { realm ->
                            realm.where(RavenMessage::class.java).equalTo("messageId", messageId).findAll().deleteAllFromRealm()
                        }
                    }
                }
            }

            realm?.executeTransactionAsync {

                val lastMessage = it.where(RavenMessage::class.java)
                        ?.equalTo("threadId", threadId)
                        ?.notEqualTo("deletedBy", FirebaseAuth.getInstance().uid)
                        ?.sort("localTimestamp", Sort.DESCENDING, "timestamp", Sort.DESCENDING)
                        ?.findFirst()

                val ravenThread = it.where(RavenThread::class.java).equalTo("threadId", threadId).findFirst()
                        ?: return@executeTransactionAsync
                ravenThread.lastMessage = lastMessage

                it.insertOrUpdate(ravenThread)
            }
        }
    }

    private fun sendMessage(messageText: String? = null, fileRef: String? = null) {

        val encryptedMessage = ThreadManager.encryptMessage(threadId, messageText)

        val message = Message()

        message.text = encryptedMessage
        message.fileRef = fileRef

        message.sentByUserId = FirebaseAuth.getInstance().uid!!
        message.sentTime = Timestamp.now()

        var users = arrayListOf(FirebaseAuth.getInstance().uid!!, userId)
        if (userId == RavenUtils.GROUP)
            users = thread?.users ?: return

        message.notDeletedBy = users
        message.sentTo = users.filterNot { it == FirebaseAuth.getInstance().uid } as ArrayList<String>

        threadManager.sendMessage(threadId, message, userId)
    }

    private fun handleCropResult(uri: Uri) {

        // sendMessage(fileRef = uri.toString())
        threadManager.sendFile(threadId, uri, null, {
            val result = it.result
            if (it.isSuccessful && result != null)
                sendMessage(fileRef = result.storage.path)
        })
    }

    @SuppressLint("CheckResult")
    private fun loadBackground(fileRef: String?, alpha: Float?) {

        val requestOptions = RequestOptions()
        requestOptions.error(R.drawable.artwork_raven)
        requestOptions.placeholder(R.drawable.artwork_raven)

        if (fileRef != null) {
            FirebaseStorageUtil().getDownloadUrl(this@ChatActivity, fileRef) {

                if (it != null) {
                    background.alpha = alpha ?: 1F
                    GlideUtilV2.loadChatBackground(this@ChatActivity, it, requestOptions, background)
                } else
                    GlideUtilV2.loadChatBackground(this@ChatActivity, null, requestOptions, background)
            }
        } else
            GlideUtilV2.loadChatBackground(this@ChatActivity, null, requestOptions, background)
    }

    private fun setMessageSelectedProperty(messageId: String, toggle: Boolean, value: Boolean = false) {

        realm.executeTransactionAsync {
            val ravenMessage = it.where(RavenMessage::class.java).equalTo("messageId", messageId).findFirst()
                    ?: return@executeTransactionAsync

            if (toggle)
                ravenMessage.selected = !ravenMessage.selected
            else
                ravenMessage.selected = value

            it.insertOrUpdate(ravenMessage)
        }
    }

    private fun setMessageSelectedPropertyForAll(select: Boolean) {

        realm.executeTransactionAsync {

            // find all messages in thread with inverted property
            val res = it.where(RavenMessage::class.java)
                    .equalTo("threadId", threadId)
                    .notEqualTo("deletedBy", FirebaseAuth.getInstance().uid)
                    .equalTo("selected", !select).findAll()

            res.forEach { mess ->
                mess.selected = select
                it.insertOrUpdate(mess)
            }
        }
    }

    private fun showDeleteDialog(flag: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.apply {

            setPositiveButton(
                    "Delete for me"
            ) { _, _ ->

                selectedMessages.forEach {
                    threadManager.deleteForCurrentUser(threadId, it.messageId)
                }
            }

            setNegativeButton(
                    "Cancel"
            ) { _, _ ->

            }

            if (flag) {
                setNeutralButton(
                        "Delete for Everyone"
                ) { _, _ ->
                    selectedMessages.forEach {
                        threadManager.deleteMessageForEveryone(threadId, it.messageId, it.fileRef, null)
                    }

                    setMessageSelectedPropertyForAll(false)
                }
            }
        }

        val alertDialog = builder.create()
        alertDialog.setMessage("Delete messages ?")
        alertDialog.show()
    }
}