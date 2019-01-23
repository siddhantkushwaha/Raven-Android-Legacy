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
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, ChatBackgroundGallery::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    private val tag: String = ChatBackgroundGallery::class.java.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_background_gallery)

        setSupportActionBar(toolbar)
        toolbar.title = "Choose a Background"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val data = ArrayList<String>()
        val adapter = ChatBackgroundGalleryAdapter(this@ChatBackgroundGallery, data)
        backgrounds.adapter = adapter

        backgrounds.setOnItemClickListener { _, _, position, _ ->

            val intent = Intent(this@ChatBackgroundGallery, EditBackground::class.java)
            intent.putExtra("fileRef", adapter.getItem(position))
            intent.putExtra("userId", getIntent().getStringExtra("userId"))
            intent.putExtra("threadId", getIntent().getStringExtra("threadId"))
            intent.putExtra("contributedBy", adapter.getItem(position))
            startActivity(intent)
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
}
