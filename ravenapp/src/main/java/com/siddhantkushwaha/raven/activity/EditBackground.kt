package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.common.utility.GsonUtils
import com.siddhantkushwaha.raven.entity.WallpaperMetadata
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil
import com.siddhantkushwaha.raven.utility.GlideUtils
import kotlinx.android.synthetic.main.activity_edit_background.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class EditBackground : AppCompatActivity() {

    companion object {
        data class IntentData(val wallpaperMetadata: WallpaperMetadata, val userId: String, val threadId: String)

        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = Intent(activity, EditBackground::class.java)
            intent.putExtra("wallpaperMetadataJson", GsonUtils.toGson(intentData.wallpaperMetadata))
            intent.putExtra("userId", intentData.userId)
            intent.putExtra("threadId", intentData.threadId)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }

        fun getIntentData(activity: Activity): IntentData {

            val intent = activity.intent
            return IntentData(
                    GsonUtils.fromGson(intent.getStringExtra("wallpaperMetadataJson"), WallpaperMetadata::class.java),
                    intent.getStringExtra("userId"),
                    intent.getStringExtra("threadId"))
        }
    }

    var wallpaperMetadata: WallpaperMetadata? = null

    var userId: String? = null
    var threadId: String? = null

    var alpha: Float = 1.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_background)

        val intentData = getIntentData(this)

        setSupportActionBar(toolbar)
        toolbar.title = "Edit Background"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        wallpaperMetadata = intentData.wallpaperMetadata

        userId = intentData.userId
        threadId = intentData.threadId

        FirebaseStorageUtil().getDownloadUrl(this@EditBackground, wallpaperMetadata!!.highResRef) {
            val requestOptions = RequestOptions()
            // requestOptions.placeholder(R.drawable.hourglass)
            // requestOptions.error(R.drawable.bug)
            GlideUtils.loadImage(this@EditBackground, it, requestOptions, background)
        }

        infoText.text = wallpaperMetadata?.info ?: "No information available."

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

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {

        // val intentData = getIntentData(this)
        // ChatBackgroundGallery.openActivity(this, true, ChatBackgroundGallery.Companion.IntentData(intentData.userId, intentData.threadId))

        onBackPressed()

        return super.onNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_accept, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_accept -> ThreadManager().changeThreadBackground(wallpaperMetadata!!.highResRef!!, alpha, threadId!!, userId!!) {
                if (it.isSuccessful) {

                    Toast.makeText(this, "Background updated.", Toast.LENGTH_SHORT).show()

                    val intentData = getIntentData(this)
                    ChatActivity.openActivity(this, true, ChatActivity.Companion.IntentData(intentData.threadId))
                }else {
                    Toast.makeText(this, "Background update failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
