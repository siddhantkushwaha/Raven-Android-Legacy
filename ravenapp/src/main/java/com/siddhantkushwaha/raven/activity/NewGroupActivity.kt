package com.siddhantkushwaha.raven.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.siddhantkushwaha.raven.R
import kotlinx.android.synthetic.main.activity_chat.*

class NewGroupActivity : AppCompatActivity() {

    companion object {
        fun openActivity(activity: Activity, finish: Boolean) {

            val intent = Intent(activity, NewGroupActivity::class.java)
            activity.startActivity(intent)
            if (finish)
                activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        toolbar.title = "New Group"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
