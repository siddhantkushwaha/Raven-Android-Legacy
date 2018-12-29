package com.siddhantkushwaha.raven.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.commonUtility.DateTimeUtils
import com.siddhantkushwaha.raven.commonUtility.GlideUtils
import com.siddhantkushwaha.raven.localEntity.RavenThread
import com.siddhantkushwaha.raven.manager.ThreadManager

import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.Exception
import java.security.GeneralSecurityException

class ThreadAdapter(private val context: Context, private val data: OrderedRealmCollection<RavenThread>) : RealmBaseAdapter<RavenThread>(data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_thread, parent, false)
        }

        val ravenThread = data[position]

        view?.findViewById<TextView>(R.id.name)!!.text = ravenThread.user?.contactName ?: ravenThread.user?.displayName ?: ravenThread.user?.phoneNumber ?: context.getString(R.string.default_name)
        GlideUtils.loadProfilePhotoCircle(context, view.findViewById(R.id.displayPicImageView), ravenThread?.user?.picUrl)

        when (ravenThread.lastMessage?.getMessageType(FirebaseAuth.getInstance().uid)) {
            1 -> {

                // sent message
                // based on whether other user has seen your message or not
                view.findViewById<ImageView>(R.id.sentMessageStatus).visibility = View.VISIBLE
                if (ravenThread.lastMessage?.seenAt != null) {
                    view.findViewById<ImageView>(R.id.sentMessageStatus).setImageDrawable(context.getDrawable(R.drawable.badge_message_status_seen))
                } else if (ravenThread.lastMessage?.timestamp != null) {
                    view.findViewById<ImageView>(R.id.sentMessageStatus).setImageDrawable(context.getDrawable(R.drawable.badge_message_status_sent))
                } else {
                    view.findViewById<ImageView>(R.id.sentMessageStatus).setImageDrawable(context.getDrawable(R.drawable.badge_message_status_pending))
                }

                view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorGreyDark))
                view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.NORMAL)
            }
            2 -> {

                // received message
                // based on whether you have seen the message or not
                view.findViewById<ImageView>(R.id.sentMessageStatus).visibility = View.GONE

                if (ravenThread.read == false) {

                    view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                    view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.BOLD)
                } else {
                    view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorGreyDark))
                    view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        var messageText = "Message Deleted."
        view.findViewById<ImageView>(R.id.messageIcon).visibility = View.GONE
        var time: DateTime? = null
        if (ravenThread?.lastMessage != null) {

            val text = ravenThread.lastMessage.text
            val fileRef = ravenThread.lastMessage.fileRef

            if (text == null && fileRef == null) {
                messageText = "Message Deleted."
                view.findViewById<ImageView>(R.id.messageIcon).visibility = View.GONE

            } else if (text == null) {
                messageText = "Photo."
                view.findViewById<ImageView>(R.id.messageIcon).visibility = View.VISIBLE

            } else if (fileRef == null) {
                messageText = decryptMessage(ravenThread.lastMessage.threadId, text)
                view.findViewById<ImageView>(R.id.messageIcon).visibility = View.GONE

            } else {
                messageText = decryptMessage(ravenThread.lastMessage.threadId, text)
                view.findViewById<ImageView>(R.id.messageIcon).visibility = View.VISIBLE
            }

            val strTime = ravenThread.lastMessage?.timestamp
                    ?: ravenThread.lastMessage?.localTimestamp
            if (strTime != null)
                time = DateTime.parse(strTime)

        }

        view.findViewById<TextView>(R.id.messageText).text = messageText

        if (time != null) {
            when {
                DateTimeUtils.isToday(time) -> view.findViewById<TextView>(R.id.messageSentTime).text = DateTimeFormat.forPattern("hh:mm:aa").print(time)
                DateTimeUtils.isYesterday(time) -> view.findViewById<TextView>(R.id.messageSentTime).text = "Yesterday"
                else -> view.findViewById<TextView>(R.id.messageSentTime).text = DateTimeFormat.forPattern("dd/MM/yy").print(time)
            }
        } else {
            view.findViewById<TextView>(R.id.messageSentTime).text = ""
        }

        return view
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