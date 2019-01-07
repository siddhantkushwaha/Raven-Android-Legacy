package com.siddhantkushwaha.raven.activity

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.EditText
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.commonUtility.UiUtil
import com.siddhantkushwaha.raven.ravenUtility.FirebaseStorageUtil
import kotlinx.android.synthetic.main.activity_image_full_screen.*
import android.util.Log
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.siddhantkushwaha.raven.ravenUtility.GlideUtils


class ImageFullScreenActivity : AppCompatActivity() {

    val tag = ImageFullScreenActivity::class.java.toString()

    val immersive = "immersive"
    val non_immersive = "non_immersive"

    var layoutState: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)

//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        layoutState = EditText(this@ImageFullScreenActivity)

        layoutState!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when (s.toString()) {
                    immersive -> {
                        UiUtil.hideSystemUi(this@ImageFullScreenActivity)
                    }
                    non_immersive -> {
                        UiUtil.showSystemUi(this@ImageFullScreenActivity)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        layoutState!!.setText(savedInstanceState?.getString("layout_state") ?: non_immersive)

        val gestureDetector = GestureDetector(this@ImageFullScreenActivity, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                when (layoutState!!.text.toString()) {
                    immersive -> {
                        layoutState!!.setText(non_immersive)
                    }
                    non_immersive -> {
                        layoutState!!.setText(immersive)
                    }
                }
                return super.onSingleTapConfirmed(e)
            }
        })

        val scaleGestureDetector = ScaleGestureDetector(this@ImageFullScreenActivity, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {

                Log.i(tag, detector.scaleFactor.toString())

                return true
            }
        })

        image.setOnTouchListener { v, event ->

            gestureDetector.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }

        val fileRef = intent.getStringExtra("key_file_ref")
        FirebaseStorageUtil().getDownloadUrl(this@ImageFullScreenActivity, fileRef) {

            GlideUtils.loadImageAsBitmap(this@ImageFullScreenActivity, it, RequestOptions(), object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    image.setImageBitmap(resource)
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putString("layout_state", layoutState!!.text.toString())
    }
}
