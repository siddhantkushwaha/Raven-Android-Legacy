package com.siddhantkushwaha.raven.utility

import android.app.Activity
import android.content.Intent

class ImageUtil {

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

            activity.startActivityForResult(chooserIntent,
                    pickImage
            );
        }
    }
}