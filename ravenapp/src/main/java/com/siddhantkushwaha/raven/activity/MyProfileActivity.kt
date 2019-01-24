package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.utility.GlideUtils
import com.siddhantkushwaha.raven.entity.User
import com.siddhantkushwaha.raven.manager.UserManager
import kotlinx.android.synthetic.main.activity_my_profile.*
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import com.siddhantkushwaha.raven.custom.CustomMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.CompoundButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.siddhantkushwaha.raven.common.utility.ActivityInfo
import com.siddhantkushwaha.raven.common.utility.Alerts
import com.siddhantkushwaha.raven.common.utility.DateTimeUtils
import com.siddhantkushwaha.raven.utilityActivity.activityRemoveDisplayPicture
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.io.File


class MyProfileActivity : AppCompatActivity() {

    companion object {
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, MyProfileActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    private val tag = MyProfileActivity::class.java.toString()

    private val pickImage = 1
    private val maxSize = 500

    private var user: User? = null
    private var userManager: UserManager? = null
    private var userEventListener: EventListener<DocumentSnapshot>? = null

    private var mMap: GoogleMap? = null
    private var mLocationPermissionGranted: Boolean? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var marker: Marker? = null

    private var privacyChangeListener: CompoundButton.OnCheckedChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setSupportActionBar(toolbar)
        toolbar.title = "My Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setGoogleMaps()

        uploadButton.setOnClickListener { _ ->
            openImageIntent()
        }

        myLocationButton.setOnClickListener { _ ->
            val latLng = LatLng(user?.userLocation?.latitude ?: 0.0, user?.userLocation?.longitude
                    ?: 0.0)
            moveCamera(latLng, 1, "You are here")
        }


        updateLocation.setOnClickListener { _ ->
            getLocation()
        }

        privacyChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->

            var privacyStatus = UserManager.ENUM_USER_PRIVACY_NONE
            if (isChecked) privacyStatus = UserManager.ENUM_USER_PRIVACY_PUBLIC

            val map = HashMap<String, Any>()
            map[UserManager.KEY_LOCATION_PRIVACY] = privacyStatus
            updateUserFields(map)
        }
        statusLocationPublic.setOnCheckedChangeListener(privacyChangeListener)

        updateLocation.setOnClickListener { _ ->
            getLocation()
        }

        submit.setOnClickListener { updateProfile() }

        user = User()
        userManager = UserManager()
        userEventListener = setUserEventListener()
        updateProfileLayout()
    }

    override fun onStart() {
        super.onStart()

        ActivityInfo.setActivityInfo(this::class.java.toString(), intent.extras)

        userManager?.startUserSyncByUserId(this@MyProfileActivity, FirebaseAuth.getInstance().uid, userEventListener)
    }

    override fun onPause() {
        super.onPause()

        ActivityInfo.setActivityInfo(null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            pickImage -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.hasExtra("remove_image") == true)
                        removeImage()
                    else
                        startCrop(data?.data ?: return)
                }
            }
            UCrop.REQUEST_CROP -> {
                handleCropResult(UCrop.getOutput(data ?: return) ?: return)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {

            0 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getLocation()
            }
        }
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

        mLocationPermissionGranted = false
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getLocation() {

        if (ContextCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val location = task.result
                    val latLng = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
                    updateLocation(latLng)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0)
        }
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

    /*---------------------------------------- IMAGE HANDLING -------------------------------------------*/

    private fun openImageIntent() {

        val otherOptions = ArrayList<Intent>()

        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"

        val removeIntent = Intent(this@MyProfileActivity, activityRemoveDisplayPicture::class.java)
        otherOptions.add(removeIntent)

        val chooserIntent = Intent.createChooser(galleryIntent, "Select Source")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, Array(otherOptions.size) { otherOptions[it] })

        startActivityForResult(chooserIntent, pickImage);
    }

    private fun startCrop(uri: Uri) {
        val uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, "display_pic_square.png")))
        uCrop?.withMaxResultSize(maxSize, maxSize)
        uCrop?.withAspectRatio(1F, 1F)

        val options = UCrop.Options()
        options.setStatusBarColor(ContextCompat.getColor(this@MyProfileActivity, R.color.colorPrimaryDark))
        options.setToolbarColor(ContextCompat.getColor(this@MyProfileActivity, R.color.colorPrimary))
        options.setActiveWidgetColor(ContextCompat.getColor(this@MyProfileActivity, R.color.colorAccent))
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        uCrop?.withOptions(options)
        uCrop?.start(this@MyProfileActivity)
    }

    private fun handleCropResult(uri: Uri) {

        userManager?.updateProfilePicture(uri) { snapshotTask ->
            if (snapshotTask.isSuccessful) {
                snapshotTask.result?.storage?.downloadUrl?.addOnCompleteListener { uriTask ->
                    val map = HashMap<String, Any>()
                    map[UserManager.KEY_PROFILE_PIC] = uriTask.result.toString()
                    updateUserFields(map)
                }
            }
        }
    }

    private fun removeImage() {

        val map = HashMap<String, Any>()
        map[UserManager.KEY_PROFILE_PIC] = FieldValue.delete()
        updateUserFields(map)

        val picUrl = user?.userProfile?.picUrl
        if (picUrl != null)
            FirebaseStorage.getInstance().getReferenceFromUrl(picUrl).delete()
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

    private fun updateProfile() {

        val map = HashMap<String, Any>()

        if (nameEditText.text.toString() != user?.userProfile?.name)
            map[UserManager.KEY_NAME] = nameEditText.text.toString()

        if (aboutEditText.text.toString() != user?.userProfile?.about)
            map[UserManager.KEY_ABOUT] = aboutEditText.text.toString()

        if (map.isNotEmpty())
            updateUserFields(map)
    }

    private fun updateLocation(latLng: LatLng) {

        val map = HashMap<String, Any>()

        map[UserManager.KEY_LOCATION_LATITUDE] = latLng.latitude
        map[UserManager.KEY_LOCATION_LONGITUDE] = latLng.longitude
        map[UserManager.KEY_LOCATION_PRIVACY] = user?.userLocation?.privacyStatus ?: UserManager.ENUM_USER_PRIVACY_PUBLIC

        map[UserManager.KEY_LOCATION_TIMESTAMP] = Timestamp.now()

        //remove this later
        map[UserManager.KEY_LOCATION_TIME] = FieldValue.delete()
        updateUserFields(map)
    }

    private fun updateProfileLayout() {

        nameEditText.setText(user?.userProfile?.name ?: getString(R.string.default_name))
        aboutEditText.setText(user?.userProfile?.about ?: getString(R.string.default_about))
        phoneEditText.setText(FirebaseAuth.getInstance().currentUser?.phoneNumber)

        GlideUtils.loadProfilePhotoCircle(this@MyProfileActivity, profilePicture, user?.userProfile?.picUrl)

        if (user?.userLocation != null) {

            mapRelativeLayout.visibility = View.VISIBLE
            val latLng = LatLng(user?.userLocation?.latitude ?: 0.0, user?.userLocation?.longitude
                    ?: 0.0)
            moveCamera(latLng, 0, "You are here")

            statusLocationPublic.isEnabled = true

            statusLocationPublic.setOnCheckedChangeListener(null)
            statusLocationPublic.isChecked = user?.userLocation?.privacyStatus == UserManager.ENUM_USER_PRIVACY_PUBLIC
            if (statusLocationPublic.isChecked)
                statusLocationPublicTextView.text = "Your location is public."
            else
                statusLocationPublicTextView.text = "Your location is private."
            statusLocationPublic.setOnCheckedChangeListener(privacyChangeListener)

            val time = DateTimeUtils.getJodaDateTime(user?.userLocation?.timestamp)
            val build = DateTimeFormat.forPattern("hh:mm a 'on' MMMMMMMMM d, yyyy").withZone(DateTimeZone.getDefault())
            lastUpdatedTextView.text = build.print(time)

        } else {

            mapRelativeLayout.visibility = View.GONE
            statusLocationPublic.isEnabled = false
            statusLocationPublic.isChecked = true
            lastUpdatedTextView.text = ""
        }
    }

    private fun updateUserFields(map: HashMap<String, Any>) {

        userManager?.updateUserFields(FirebaseAuth.getInstance().uid, map) { task ->
            var message = "Failed to update profile."
            if (task.isSuccessful) {
                message = "Successfully updated profile."
            }
            Alerts.showToast(this@MyProfileActivity, message, 2000)
        }
    }
}