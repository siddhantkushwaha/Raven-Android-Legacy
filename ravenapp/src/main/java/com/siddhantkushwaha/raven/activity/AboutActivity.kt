package com.siddhantkushwaha.raven.activity

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.siddhantkushwaha.raven.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.lang.Exception

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R
                .layout.activity_about)

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

//        chatBackground.setOnClickListener {
//            val intent = Intent(this@AboutActivity, ChatBackgroundGallery::class.java)
//            startActivity(intent)
//        }
    }
}
