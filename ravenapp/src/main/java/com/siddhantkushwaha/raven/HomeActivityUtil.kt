package com.siddhantkushwaha.raven

import com.siddhantkushwaha.raven.manager.UserManager
import org.joda.time.DateTime
import java.util.*

class HomeActivityUtil {

    companion object {
        fun setUserMetaData(userManager: UserManager, userId: String) {

            val map = HashMap<String, Any>()
            map["buildType"] = BuildConfig.BUILD_TYPE
            map["versionName"] = BuildConfig.VERSION_NAME
            map["versionCode"] = BuildConfig.VERSION_CODE
            map["timestamp"] = DateTime().toString()

            userManager.setUserMetaData(userId, map) {
                it.exception?.printStackTrace()
            }
        }
    }
}