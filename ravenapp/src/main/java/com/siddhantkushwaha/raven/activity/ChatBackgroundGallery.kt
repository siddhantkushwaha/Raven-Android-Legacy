package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.activity.main.ChatActivity
import com.siddhantkushwaha.raven.adapter.ChatBackgroundGalleryAdapter
import com.siddhantkushwaha.raven.entity.WallpaperMetadata
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.utility.FirebaseUtils
import kotlinx.android.synthetic.main.activity_chat_background_gallery.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class ChatBackgroundGallery : AppCompatActivity() {

    companion object {
        data class IntentData(val userId: String, val threadId: String)

        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = Intent(activity, ChatBackgroundGallery::class.java)
            intent.putExtra("userId", intentData.userId)
            intent.putExtra("threadId", intentData.threadId)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }

        fun getIntentData(activity: Activity): IntentData {

            val intent = activity.intent
            return IntentData(intent.getStringExtra("userId"),
                    intent.getStringExtra("threadId"))
        }
    }

    private val tag: String = ChatBackgroundGallery::class.java.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_background_gallery)

        val intentData = getIntentData(this)

        setSupportActionBar(toolbar)
        toolbar.title = "Choose a Background"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        resetBackground.setOnClickListener {

            ThreadManager().deleteThreadBackground(intentData.threadId, intentData.userId) { task ->

                if (task.isSuccessful) {

                    Toast.makeText(this, "Background updated.", Toast.LENGTH_SHORT).show()
                    ChatActivity.openActivity(this, true, ChatActivity.Companion.IntentData(intentData.threadId))
                } else {
                    Toast.makeText(this, "Background update failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val data = ArrayList<WallpaperMetadata>()
        val adapter = ChatBackgroundGalleryAdapter(this@ChatBackgroundGallery, data)
        backgrounds.adapter = adapter

        backgrounds.setOnItemClickListener { _, _, position, _ ->

            EditBackground.openActivity(this@ChatBackgroundGallery, false, EditBackground.Companion.IntentData(
                    adapter.getItem(position),
                    intentData.userId,
                    intentData.threadId
            ))
        }

        FirebaseUtils.getRealtimeDb(true).getReference("wallpaperIndex").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                p0.toException().printStackTrace()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                updateData(p0, data, adapter)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                updateData(p0, data, adapter)
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                updateData(p0, data, adapter)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                updateData(p0, data, adapter)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {

        // val intentData = getIntentData(this)
        // ChatActivity.openActivity(this, true, ChatActivity.Companion.IntentData(intentData.userId, intentData.threadId))

        onBackPressed()

        return super.onSupportNavigateUp()
    }

    private fun updateData(snap: DataSnapshot, data: ArrayList<WallpaperMetadata>, adapter: ChatBackgroundGalleryAdapter) {

        try {
            val wallpaperMetadata1: WallpaperMetadata = snap.getValue(WallpaperMetadata::class.java)!!
            val wallpaperMetadata2: WallpaperMetadata? = data.find { it.lowResRef == wallpaperMetadata1.lowResRef }

            if (wallpaperMetadata2 == null)
                data.add(wallpaperMetadata1)
            else
                wallpaperMetadata2.cloneObject(wallpaperMetadata1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        adapter.notifyDataSetChanged()
    }
}
