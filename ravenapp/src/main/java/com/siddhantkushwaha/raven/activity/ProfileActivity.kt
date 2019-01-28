package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.siddhantkushwaha.android.thugtools.thugtools.utility.ActivityInfo
import com.siddhantkushwaha.android.thugtools.thugtools.utility.UiUtil
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.common.utility.DateTimeUtils
import com.siddhantkushwaha.raven.custom.CustomMapFragment
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.manager.UserManager
import com.siddhantkushwaha.raven.utility.GlideUtils
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.layout_profile_content_scrolling.*
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

class ProfileActivity : AppCompatActivity() {

    companion object {
        data class IntentData(val userId: String)

        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("userId", intentData.userId)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }

        fun getIntentData(activity: Activity): IntentData {

            val intent = activity.intent
            return IntentData(intent.getStringExtra("userId"))
        }
    }

    private val tag = ProfileActivity::class.java.toString()

    private var userId: String? = null

    private var user: User? = null
    private var userManager: UserManager? = null
    private var userEventListener: EventListener<DocumentSnapshot>? = null

    private var mMap: GoogleMap? = null
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        val intentData = getIntentData(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userId = intentData.userId

        setGoogleMaps()

        myLocationButton.setOnClickListener {
            val latLng = LatLng(user?.userLocation?.latitude ?: 0.0, user?.userLocation?.longitude
                    ?: 0.0)
            moveCamera(latLng, 0, "Last seen here.")
        }

        user = User()
        userManager = UserManager()
        userEventListener = setUserEventListener()
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        userManager?.startUserSyncByUserId(this@ProfileActivity, userId, userEventListener)
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    /*------------------------------------------- MAP STUFF ---------------------------------------------*/

    private fun setGoogleMaps() {

        val mapFragment = mapFragment as CustomMapFragment
        mapFragment.setListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        mapFragment.getMapAsync { googleMap -> mMap = googleMap }
    }


    private fun moveCamera(latLng: LatLng, flag: Int, message: String) {

        if (flag == 0)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
        else
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))

        marker?.remove()
        val markerOptions = MarkerOptions()
        markerOptions.title(message)
        markerOptions.position(latLng)
        marker = mMap?.addMarker(markerOptions)
    }

    /*---------------------------------------------------------------------------------------------------*/

    private fun setUserEventListener(): EventListener<DocumentSnapshot>? {

        return EventListener { snapshot, _ ->

            if (snapshot != null && snapshot.exists()) {
                val tempUser: User? = snapshot.toObject(User::class.java)
                user?.cloneObject(tempUser)
            }
            updateProfileLayout()
        }
    }

    private fun updateProfileLayout() {

        collapsingToolbar.title = user?.userProfile?.name ?: getString(R.string.default_name)
        aboutTextView.text = user?.userProfile?.about ?: getString(R.string.default_about)

        GlideUtils.loadProfilePhotoSquare(this@ProfileActivity, imageRelativeLayout, user?.userProfile?.picUrl)
        GlideUtils.loadImageAsBitmap(this@ProfileActivity, user?.userProfile?.picUrl, RequestOptions(), object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                Palette.from(resource).generate {
                    val swatch = it?.darkMutedSwatch
                    if (swatch != null) {
                        collapsingToolbar.setContentScrimColor(swatch.rgb)
                        collapsingToolbar.setBackgroundColor(swatch.rgb)
                        UiUtil.setStatusBarColor(this@ProfileActivity, UiUtil.manipulateColor(swatch.rgb, 0.8f), UiUtil.DARK)
                    }
                }
            }
        })

        if (user?.userLocation != null) {

            if (user?.userLocation?.privacyStatus == UserManager.ENUM_USER_PRIVACY_PUBLIC) {

                mapRelativeLayout.visibility = View.VISIBLE
                val latLng = LatLng(user?.userLocation?.latitude
                        ?: 0.0, user?.userLocation?.longitude
                        ?: 0.0)
                moveCamera(latLng, 0, "Last seen here.")

                val time = DateTimeUtils.getJodaDateTime(user?.userLocation?.timestamp)
                val build = DateTimeFormat.forPattern("hh:mm a 'on' MMMMMMMMM d, yyyy").withZone(DateTimeZone.getDefault())
                locationInfoTextView.text = build.print(time)
            } else {
                mapRelativeLayout.visibility = View.GONE
                locationInfoTextView.text = "This information is private."
            }
        } else {
            mapRelativeLayout.visibility = View.GONE
            locationInfoTextView.text = "Never Updated."
        }
    }
}