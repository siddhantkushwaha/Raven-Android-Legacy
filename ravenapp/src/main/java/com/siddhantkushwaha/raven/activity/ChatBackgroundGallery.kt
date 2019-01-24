package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.ChatBackgroundGalleryAdapter
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

        val data = ArrayList<String>()
        val adapter = ChatBackgroundGalleryAdapter(this@ChatBackgroundGallery, data)
        backgrounds.adapter = adapter

        backgrounds.setOnItemClickListener { _, _, position, _ ->

            EditBackground.openActivity(this@ChatBackgroundGallery, false, EditBackground.Companion.IntentData(
                    adapter.getItem(position),
                    intentData.userId,
                    intentData.threadId,
                    "Shobhit Malarya"
            ))
        }

        FirebaseUtils.getFirestoreDb(true).collection("storageIndex").document("abstractWallpapers").get()
                .addOnSuccessListener {

                    val list = it.get("list") as? ArrayList<String>
                    list?.forEach { fileRef ->
                        if (!data.contains(fileRef)) {
                            data.add(fileRef)
                            adapter.notifyDataSetChanged()
                        }
                    }

                }
                .addOnFailureListener {
                    it.printStackTrace()
                }

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
}
