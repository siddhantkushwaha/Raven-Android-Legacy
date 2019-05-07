package com.siddhantkushwaha.raven.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.utility.GlideUtil
import com.siddhantkushwaha.raven.utility.JodaTimeUtil
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter
import kotlinx.android.synthetic.main.layout_thread.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.security.GeneralSecurityException

class ThreadAdapter(private val context: Context, private val data: OrderedRealmCollection<RavenThread>) : RealmBaseAdapter<RavenThread>(data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val listItem: View = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.layout_thread, parent, false)

        val ravenThread = data[position]

        var threadTitle: String? = null
        var threadPic: String? = null
        when (ravenThread.isGroup) {

            true -> {
                threadTitle = ravenThread.groupName
                threadPic = ravenThread.picUrl
            }

            false -> {
                threadTitle = ravenThread.user?.contactName ?: ravenThread.user?.displayName
                        ?: ravenThread.user?.phoneNumber ?: context.getString(R.string.default_name)
                threadPic = ravenThread?.user?.picUrl
            }
        }

        listItem.name.text = threadTitle
        GlideUtil.loadProfilePhotoCircle(context, listItem.displayPic, threadPic)

        when (ravenThread.lastMessage) {
            null -> {
                listItem.status.visibility = View.GONE
                listItem.icon.visibility = View.GONE
                setTextViewStyle(listItem.text, R.color.colorGreyDark, Typeface.NORMAL)
                listItem.text.text = "No Messages."
            }

            else -> {

                val type = ravenThread.lastMessage.getMessageType(FirebaseAuth.getInstance().uid!!)
                when (type) {
                    1, 2 -> {
                        listItem.status.visibility = View.VISIBLE

                        when {
                            ravenThread.lastMessage.isSeenByAll -> listItem.status.text = "seen"
                            ravenThread.lastMessage.timestamp != null -> listItem.status.text = "sent"
                            else -> listItem.status.text = ""
                        }
                        setTextViewStyle(listItem.text, R.color.colorGreyDark, Typeface.NORMAL)
                    }

                    3, 4 -> {
                        listItem.status.visibility = View.GONE
                        if (ravenThread.lastMessage.getSeenByUserId(FirebaseAuth.getInstance().uid!!) == null)
                            setTextViewStyle(listItem.text, R.color.colorBlack, Typeface.BOLD)
                        else
                            setTextViewStyle(listItem.text, R.color.colorGreyDark, Typeface.NORMAL)
                    }
                }
                when (type) {

                    1, 3 -> {
                        listItem.icon.visibility = View.GONE
                    }

                    2, 4 -> {
                        listItem.icon.visibility = View.VISIBLE
                    }
                }

                val text = ravenThread.lastMessage.text
                val fileRef = ravenThread.lastMessage.fileRef
                val uploadUri = ravenThread.lastMessage.uploadUri

                val messageText = if (text == null && fileRef == null && uploadUri == null) {
                    "Message Deleted."
                } else if (text == null) {
                    "Photo."
                } else if (fileRef == null) {
                    decryptMessage(ravenThread.lastMessage.threadId, text)
                } else {
                    decryptMessage(ravenThread.lastMessage.threadId, text)
                }

                listItem.text.text = messageText
            }
        }

        if (ravenThread.timestamp != null) {
            val time = DateTime.parse(ravenThread.timestamp)
            when {
                JodaTimeUtil.isToday(time) -> listItem.timestamp.text = DateTimeFormat.forPattern("hh:mm:aa").print(time)
                JodaTimeUtil.isYesterday(time) -> listItem.timestamp.text = "Yesterday"
                else -> listItem.timestamp.text = DateTimeFormat.forPattern("dd/MM/yy").print(time)
            }
        } else
            listItem.timestamp.text = ""

        return listItem
    }

    private fun setTextViewStyle(textView: TextView, color: Int, typeface: Int) {

        textView.setTextColor(ContextCompat.getColor(context, color))
        textView.setTypeface(null, typeface)
    }

    private fun decryptMessage(key: String, message: String): String {
        return try {
            ThreadManager.decryptMessage(key, message)
        } catch (e: GeneralSecurityException) {
            "Couldn't Decrypt."
        } catch (e: Exception) {
            "There was a problem."
        }
    }
}