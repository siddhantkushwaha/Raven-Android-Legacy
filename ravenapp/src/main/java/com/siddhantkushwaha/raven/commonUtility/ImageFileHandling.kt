package com.siddhantkushwaha.raven.commonUtility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.Log
import com.siddhantkushwaha.raven.R
import com.yalantis.ucrop.UCrop
import java.io.File

class ImageFileHandling {

    companion object {

        val pickImage = 183

        fun openImageIntent(activity: Activity) {

            val otherOptions = ArrayList<Intent>()

            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"

            // val removeIntent = Intent(context, activityRemoveDisplayPicture::class.java)
            // otherOptions.add(removeIntent)

            val chooserIntent = Intent.createChooser(galleryIntent, "Select Source")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, Array(otherOptions.size) { otherOptions[it] })

            activity.startActivityForResult(chooserIntent, pickImage);
        }

        fun startCrop(activity: Activity, uri: Uri) {
            val uCrop = UCrop.of(uri, Uri.fromFile(File(activity.cacheDir, "file_to_send.png")))
            uCrop.withMaxResultSize(1024, 1024)

            val options = UCrop.Options()
            options.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark))
            options.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            options.setActiveWidgetColor(ContextCompat.getColor(activity, R.color.colorAccent))
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            uCrop?.withOptions(options)
            uCrop?.start(activity)
        }
    }
}