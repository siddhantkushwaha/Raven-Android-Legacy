package com.siddhantkushwaha.raven.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.adapter.ChatBackgroundGalleryAdapter
import com.siddhantkushwaha.raven.ravenUtility.FirebaseUtils
import kotlinx.android.synthetic.main.activity_chat_background_gallery.*

class ChatBackgroundGallery : AppCompatActivity() {

    private val tag: String = ChatBackgroundGallery::class.java.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_background_gallery)

        val data = ArrayList<String>()
        val adapter = ChatBackgroundGalleryAdapter(this@ChatBackgroundGallery, data)
        backgrounds.adapter = adapter

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
