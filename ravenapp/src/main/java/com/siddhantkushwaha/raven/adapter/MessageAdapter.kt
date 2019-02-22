package com.siddhantkushwaha.raven.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.realm.entity.RavenMessage
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.utility.Common
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil
import com.siddhantkushwaha.raven.utility.GlideUtilV2
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.security.GeneralSecurityException

class MessageAdapter(private val context: Context, private val ravenThread: RavenThread, data: OrderedRealmCollection<RavenMessage>, autoUpdate: Boolean, private val onClickListener: OnClickListener) : RealmRecyclerViewAdapter<RavenMessage, RecyclerView.ViewHolder>(data, autoUpdate) {

    override fun getItemViewType(position: Int): Int {
        return data!![position].getMessageType(FirebaseAuth.getInstance().uid!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_sent_simple, parent, false)
                return SentMessageViewHolder(context, view, onClickListener)
            }

            2 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_sent_image, parent, false)
                return SentMessageWithImageViewHolder(context, view, onClickListener)
            }

            3 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_received_simple, parent, false)
                return ReceivedMessageViewHolder(context, view, ravenThread, onClickListener)
            }

            4 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_received_image, parent, false)
                return ReceivedMessageWithImageViewHolder(context, view, ravenThread, onClickListener)
            }

            -1 -> {
                // TODO TBD
            }
        }

        throw RuntimeException("Message didn't match any category.")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val ravenMessage = data?.get(position)!!
        when (holder.itemViewType) {

            1 -> (holder as SentMessageViewHolder).bind(ravenMessage)
            2 -> (holder as SentMessageWithImageViewHolder).bind(ravenMessage)
            3 -> (holder as ReceivedMessageViewHolder).bind(ravenMessage)
            4 -> (holder as ReceivedMessageWithImageViewHolder).bind(ravenMessage)
        }
    }

    private class SentMessageViewHolder(private val context: Context, itemView: View, private val onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            setMessageText(text, ravenMessage.threadId, ravenMessage.text)

            setMessageTimeAndStatus(time, ravenMessage.timestamp, ravenMessage.isSeenByAll)
            setProperties(itemView, ravenMessage)
            setListener(itemView, onClickListener, ravenMessage)
        }
    }

    private class SentMessageWithImageViewHolder(private val context: Context, itemView: View, private val onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val image: ImageView = itemView.findViewById(R.id.image)
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            setImage(context, image, text, ravenMessage.fileRef)

            if (ravenMessage.text != null) {
                text.visibility = View.VISIBLE
                setMessageText(text, ravenMessage.threadId, ravenMessage.text)
            } else
                text.visibility = View.GONE

            setMessageTimeAndStatus(time, ravenMessage.timestamp, ravenMessage.isSeenByAll)
            setProperties(itemView, ravenMessage)
            setListener(itemView, onClickListener, ravenMessage)
        }
    }

    private class ReceivedMessageViewHolder(private val context: Context, itemView: View, private val ravenThread: RavenThread, private val onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val body: LinearLayout = itemView.findViewById(R.id.body)
            val name: TextView = itemView.findViewById(R.id.name)
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            setName(ravenThread.isGroup, name, "siddhant", Color.RED)
            setMessageText(text, ravenMessage.threadId, ravenMessage.text, body, name)

            setMessageTimeAndStatus(time, ravenMessage.timestamp)
            setProperties(itemView, ravenMessage)
            setListener(itemView, onClickListener, ravenMessage)
        }
    }

    private class ReceivedMessageWithImageViewHolder(private val context: Context, itemView: View, private val ravenThread: RavenThread, private val onClickListener: OnClickListener) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val image: ImageView = itemView.findViewById(R.id.image)
            val body: LinearLayout = itemView.findViewById(R.id.body)
            val name: TextView = itemView.findViewById(R.id.name)
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            setImage(context, image, text, ravenMessage.fileRef)

            setName(ravenThread.isGroup, name, "siddhant", Color.RED)

            if (ravenMessage.text != null) {
                text.visibility = View.VISIBLE
                setMessageText(text, ravenMessage.threadId, ravenMessage.text, body, name)
            } else
                text.visibility = View.GONE

            setMessageTimeAndStatus(time, ravenMessage.timestamp)
            setProperties(itemView, ravenMessage)
            setListener(itemView, onClickListener, ravenMessage)
        }
    }

    companion object {

        fun setProperties(itemView: View, ravenMessage: RavenMessage) {

            if (ravenMessage.selected)
                itemView.setBackgroundResource(R.color.colorMessageSelected)
            else
                itemView.setBackgroundResource(android.R.color.transparent)
        }

        fun setImage(context: Context, imageView: ImageView, text: TextView, fileRef: String?) {

            if (fileRef != null)
                FirebaseStorageUtil.getDownloadUrl(context, fileRef) { url ->
                    GlideUtilV2.loadImageAsBitmap(context, url, RequestOptions(), object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            imageView.setImageBitmap(resource)
                            /*Palette.from(resource).generate {
                                        val swatch = it?.darkVibrantSwatch
                                        if (swatch != null)
                                            text.backgroundTintList = Common.getColorStateList(swatch.rgb)
                                        else
                                            text.backgroundTintList = null
                                    }*/
                        }
                    })
                }
        }

        fun setName(isGroup: Boolean, nameTextView: TextView, name: String, color: Int) {

            if (isGroup) {
                nameTextView.visibility = View.VISIBLE
                nameTextView.text = name
                nameTextView.setTextColor(color)
            } else
                nameTextView.visibility = View.GONE
        }

        fun setMessageText(text: TextView, threadId: String, encryptedText: String?, body: LinearLayout? = null, name: TextView? = null) {

            val colorStateList = Common.getColorStateList(Color.rgb(220, 0, 0))
            try {
                setMessageTextUtil(text, body, ThreadManager.decryptMessage(threadId, encryptedText), Color.WHITE, Color.BLACK, if (body == null) null else Common.getColorStateList(Color.WHITE))
            } catch (e: GeneralSecurityException) {
                setMessageTextUtil(text, body, "Couldn't decrypt.", Color.WHITE, Color.WHITE, colorStateList)
            } catch (e: NullPointerException) {
                setMessageTextUtil(text, body, "Message Deleted.", Color.WHITE, Color.WHITE, colorStateList)
            } catch (e: Exception) {
                setMessageTextUtil(text, body, "There was a problem.", Color.WHITE, Color.WHITE, colorStateList)
            }
        }

        fun setMessageTextUtil(text: TextView, body: LinearLayout?, message: String, color1: Int, color2: Int, colorStateList: ColorStateList?) {

            text.text = message
            text.setTextColor(if (body == null) color1 else color2)
            (body ?: text).backgroundTintList = colorStateList
        }

        fun setMessageTimeAndStatus(time: TextView, timestamp: String?, seenByAll: Boolean? = null) {

            if (timestamp != null) {
                time.visibility = View.VISIBLE
                time.text = DateTimeFormat.forPattern("hh:mm a").print(DateTime.parse(timestamp))

                if (seenByAll != null) {
                    if (seenByAll) {
                        time.setTextColor(Color.BLACK)
                        time.backgroundTintList = Common.getColorStateList(Color.rgb(0, 220, 0))
                    } else {
                        time.setTextColor(Color.WHITE)
                        time.backgroundTintList = null
                    }
                }

            } else
                time.visibility = View.GONE
        }

        fun setListener(itemView: View, onClickListener: OnClickListener, ravenMessage: RavenMessage) {

            itemView.setOnClickListener {
                onClickListener.onClick(ravenMessage)
            }

            itemView.setOnLongClickListener {
                onClickListener.onLongClick(ravenMessage)
                true
            }
        }
    }

    interface OnClickListener {
        fun onClick(ravenMessage: RavenMessage)
        fun onLongClick(ravenMessage: RavenMessage)
    }
}