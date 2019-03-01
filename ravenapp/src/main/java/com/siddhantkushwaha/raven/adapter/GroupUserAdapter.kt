package com.siddhantkushwaha.raven.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.siddhantkushwaha.nuttertools.GsonUtil
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.realm.entity.RavenUser
import com.siddhantkushwaha.raven.utility.GlideUtilV2
import io.realm.RealmList
import kotlinx.android.synthetic.main.layout_user.view.*

class GroupUserAdapter(private val context: Context, private val users: RealmList<RavenUser>, private val ravenThread: RavenThread) : BaseAdapter() {

    override fun getItem(position: Int): RavenUser {
        return users[position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return users.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val listItem: View = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.layout_user, parent, false)

        val ravenUser = users[position]!!

        GlideUtilV2.loadProfilePhotoCircle(context, listItem.displayPic, ravenUser.picUrl)

        listItem.name.text = ravenUser.contactName ?: ravenUser.displayName ?: ravenUser.phoneNumber
                ?: context.getString(R.string.default_name)
        listItem.about.text = ravenUser.about ?: context.getString(R.string.default_about)

        val map = GsonUtil.fromGson<HashMap<String, String>>(ravenThread.permissions, HashMap::class.java)
        if (map != null && map[ravenUser.userId] == "admin") {
            listItem.permission.visibility = View.VISIBLE
        } else
            listItem.permission.visibility = View.GONE

        return listItem
    }
}