package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import com.bumptech.glide.request.RequestOptions
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil
import com.siddhantkushwaha.raven.utility.GlideUtils
import kotlinx.android.synthetic.main.activity_edit_background.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class EditBackground : AppCompatActivity() {

    companion object {
        data class IntentData(val fileRef: String, val userId: String, val threadId: String, val contributedBy: String)
//        fun openActivity(activity: Activity, finish: Boolean, fileRef: String, userId: String, threadId: String, contributedBy: String) {
//
//            val intent = Intent(activity, EditBackground::class.java)
//            intent.putExtra("fileRef", fileRef)
//            intent.putExtra("userId", userId)
//            intent.putExtra("threadId", threadId)
//            intent.putExtra("contributedBy", contributedBy)
//            activity.startActivity(intent)
//            if (finish)
//                activity.finish()
//        }

        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = Intent(activity, EditBackground::class.java)
            intent.putExtra("fileRef", intentData.fileRef)
            intent.putExtra("userId", intentData.userId)
            intent.putExtra("threadId", intentData.threadId)
            intent.putExtra("contributedBy", intentData.contributedBy)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }

        fun getIntentData(activity: Activity): IntentData {

            val intent = activity.intent
            return IntentData(intent.getStringExtra("fileRef"),
                    intent.getStringExtra("userId"),
                    intent.getStringExtra("threadId"),
                    intent.getStringExtra("contributedBy"))
        }
    }

    var fileRef: String? = null
    var userId: String? = null
    var threadId: String? = null

    var alpha: Float = 1.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_background)

        setSupportActionBar(toolbar)
        toolbar.title = "Edit Background"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fileRef = intent.getStringExtra("fileRef")

        userId = intent.getStringExtra("userId")
        threadId = intent.getStringExtra("threadId")

        val contributedBy = intent.getStringExtra("contributedBy")

        if (fileRef == null) {
            finish()
        }

        FirebaseStorageUtil().getDownloadUrl(this@EditBackground, fileRef) {
            GlideUtils.loadImage(this@EditBackground, it, RequestOptions(), background)
        }
//        contributedByText.text = "Contributed By - $contributedBy"
        contributedByText.text = "Contributed By - Shobhit Malarya"

        alphaSeeker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                Log.i(EditBackground::class.java.toString(), progress.toString())
                alpha = 1 - progress / 100.0F
                Log.i(EditBackground::class.java.toString(), alpha.toString())

                background.alpha = alpha
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_accept, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_accept -> {

                ThreadManager().changeThreadBackground(fileRef!!, alpha, threadId!!, userId!!, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
