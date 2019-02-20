package com.siddhantkushwaha.raven.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.utility.GlideUtilV2
import com.siddhantkushwaha.raven.utility.JodaTimeUtilV2
import io.realm.OrderedRealmCollection
import io.realm.RealmBaseAdapter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.security.GeneralSecurityException

class ThreadAdapter(private val context: Context, private val data: OrderedRealmCollection<RavenThread>) : RealmBaseAdapter<RavenThread>(data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_thread, parent, false)
        }

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

        view?.findViewById<TextView>(R.id.name)!!.text = threadTitle
        GlideUtilV2.loadProfilePhotoCircle(context, view.findViewById(R.id.displayPicImageView), threadPic)

        if (ravenThread.lastMessage == null) {

            view.findViewById<ImageView>(R.id.sentMessageStatus).visibility = View.GONE
            view.findViewById<ImageView>(R.id.messageIcon).visibility = View.GONE

            view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorGreyDark))
            view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.NORMAL)

            view.findViewById<TextView>(R.id.messageText).text = "No Messages."
        } else {

            when (ravenThread.lastMessage.getMessageType(FirebaseAuth.getInstance().uid!!)) {
                1 -> {
                    // sent message

                    view.findViewById<ImageView>(R.id.sentMessageStatus).visibility = View.VISIBLE
                    when {
                        ravenThread.lastMessage.isSeenByAll -> view.findViewById<ImageView>(R.id.sentMessageStatus).setImageDrawable(context.getDrawable(R.drawable.badge_message_status_seen))
                        ravenThread.lastMessage.timestamp != null -> view.findViewById<ImageView>(R.id.sentMessageStatus).setImageDrawable(context.getDrawable(R.drawable.badge_message_status_sent))
                        else -> view.findViewById<ImageView>(R.id.sentMessageStatus).setImageDrawable(context.getDrawable(R.drawable.badge_message_status_pending))
                    }

                    if (ravenThread.lastMessage.fileRef != null)
                        view.findViewById<ImageView>(R.id.messageIcon).visibility = View.VISIBLE
                    else
                        view.findViewById<ImageView>(R.id.messageIcon).visibility = View.GONE


                    view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorGreyDark))
                    view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.NORMAL)
                }

                2 -> {
                    //received messaged

                    view.findViewById<ImageView>(R.id.sentMessageStatus).visibility = View.GONE

                    if (ravenThread.lastMessage.getSeenByUserId(FirebaseAuth.getInstance().uid!!) == null) {
                        view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                        view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.BOLD)
                    } else {
                        view.findViewById<TextView>(R.id.messageText).setTextColor(ContextCompat.getColor(context, R.color.colorGreyDark))
                        view.findViewById<TextView>(R.id.messageText).setTypeface(null, Typeface.NORMAL)
                    }
                }
            }

            val text = ravenThread.lastMessage.text
            val fileRef = ravenThread.lastMessage.fileRef

            val messageText = if (text == null && fileRef == null) {
                "Message Deleted."
            } else if (text == null) {
                "Photo."
            } else if (fileRef == null) {
                decryptMessage(ravenThread.lastMessage.threadId, text)
            } else {
                decryptMessage(ravenThread.lastMessage.threadId, text)
            }

            view.findViewById<TextView>(R.id.messageText).text = messageText
        }

        if (ravenThread.timestamp != null) {
            val time = DateTime.parse(ravenThread.timestamp)
            when {
                JodaTimeUtilV2.isToday(time) -> view.findViewById<TextView>(R.id.messageSentTime).text = DateTimeFormat.forPattern("hh:mm:aa").print(time)
                JodaTimeUtilV2.isYesterday(time) -> view.findViewById<TextView>(R.id.messageSentTime).text = "Yesterday"
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