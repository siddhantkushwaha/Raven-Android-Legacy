package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.siddhantkushwaha.raven.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class AboutActivity : AppCompatActivity() {

    companion object {
        data class IntentData(val dummy: String)
        fun openActivity(activity: Activity, finish: Boolean, intentData: IntentData) {

            val intent = Intent(activity, AboutActivity::class.java)
            intent.putExtra("dummy", intentData.dummy)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }

        fun getIntentData(activity: Activity): IntentData {

            val intent = activity.intent
            return IntentData(intent.getStringExtra("dummy"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R
                .layout.activity_about)

        val intentData = getIntentData(this)

        setSupportActionBar(toolbar)
        toolbar.title = "About"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        versionName.text = "v${packageManager.getPackageInfo(packageName, 0).versionName}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            versionCode.text = packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
        else {
            try {
                versionCode.text = packageManager.getPackageInfo(packageName, 0).versionCode.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
