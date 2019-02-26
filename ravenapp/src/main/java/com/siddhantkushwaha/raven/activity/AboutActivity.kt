package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.siddhantkushwaha.raven.BuildConfig
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.utility.Common
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class AboutActivity : AppCompatActivity() {

    companion object {
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, AboutActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R
                .layout.activity_about)

        setSupportActionBar(toolbar)
        toolbar.title = "About"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cardView.setOnClickListener {
            startActivity(Common.getPlaystoreIntent(BuildConfig.APPLICATION_ID))
        }

        versionName.text = "v${BuildConfig.VERSION_NAME}"
        versionCode.text = "$versionCode-{BuildConfig.VERSION_CODE.toString()}"
    }
}
