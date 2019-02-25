package com.siddhantkushwaha.raven.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.siddhantkushwaha.nuttertools.JodaTimeUtil
import com.siddhantkushwaha.raven.R
import com.siddhantkushwaha.raven.manager.ThreadManager
import com.siddhantkushwaha.raven.realm.entity.RavenMessage
import com.siddhantkushwaha.raven.realm.entity.RavenThread
import com.siddhantkushwaha.raven.utility.Common
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil
import com.siddhantkushwaha.raven.utility.GlideUtilV2
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import kotlinx.android.synthetic.main.layout_message_banner.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.security.GeneralSecurityException
import java.util.*
import kotlin.collections.HashMap

class MessageAdapter(private val context: Context, private val ravenThreadResult: RealmResults<RavenThread>, data: OrderedRealmCollection<RavenMessage>, autoUpdate: Boolean, private val onClickListener: OnClickListener) : RealmRecyclerViewAdapter<RavenMessage, RecyclerView.ViewHolder>(data, autoUpdate) {

    private val colorsForUsers = HashMap<String, Int>()
    private val timeBanner = HashMap<String, String>()

    override fun getItemViewType(position: Int): Int {
        return data!![position].getMessageType(FirebaseAuth.getInstance().uid!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_sent_simple, parent, false)
                return SentMessageViewHolder(this@MessageAdapter, view)
            }

            2 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_sent_image, parent, false)
                return SentMessageWithImageViewHolder(this@MessageAdapter, view)
            }

            3 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_received_simple, parent, false)
                return ReceivedMessageViewHolder(this@MessageAdapter, view)
            }

            4 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message_received_image, parent, false)
                return ReceivedMessageWithImageViewHolder(this@MessageAdapter, view)
            }

            -1 -> {
                // TODO TBD
            }
        }

        throw RuntimeException("Message didn't match any category.")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val ravenMessage = data?.get(position)!!
        val curTime = ravenMessage.timestamp ?: ravenMessage.localTimestamp

        if (curTime != null && position == 0) {
            timeBanner[ravenMessage.messageId] = curTime
        } else {
            val rvp = data?.get(position - 1)!!
            val preTime = rvp.timestamp ?: rvp.localTimestamp
            if (curTime != null && preTime != null)
                if (JodaTimeUtil.dateCmp(DateTime.parse(curTime), DateTime.parse(preTime)) != 0)
                    timeBanner[ravenMessage.messageId] = curTime
        }

        when (holder.itemViewType) {

            1 -> (holder as SentMessageViewHolder).bind(ravenMessage)
            2 -> (holder as SentMessageWithImageViewHolder).bind(ravenMessage)
            3 -> (holder as ReceivedMessageViewHolder).bind(ravenMessage)
            4 -> (holder as ReceivedMessageWithImageViewHolder).bind(ravenMessage)
        }
    }

    private class SentMessageViewHolder(val messageAdapter: MessageAdapter, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            messageAdapter.setMessageText(text, ravenMessage.text)

            messageAdapter.setMessageTimeAndStatus(time, ravenMessage.timestamp, ravenMessage.isSeenByAll)
            messageAdapter.setProperties(itemView, ravenMessage)
            messageAdapter.setListener(itemView, ravenMessage)
        }
    }

    private class SentMessageWithImageViewHolder(val messageAdapter: MessageAdapter, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val image: ImageView = itemView.findViewById(R.id.image)
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            messageAdapter.setImage(image, ravenMessage.fileRef)

            if (ravenMessage.text != null) {
                text.visibility = View.VISIBLE
                messageAdapter.setMessageText(text, ravenMessage.text)
            } else
                text.visibility = View.GONE

            messageAdapter.setMessageTimeAndStatus(time, ravenMessage.timestamp, ravenMessage.isSeenByAll)
            messageAdapter.setProperties(itemView, ravenMessage)
            messageAdapter.setListener(itemView, ravenMessage)
        }
    }

    private class ReceivedMessageViewHolder(val messageAdapter: MessageAdapter, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val body: LinearLayout = itemView.findViewById(R.id.body)
            val name: TextView = itemView.findViewById(R.id.name)
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            messageAdapter.setName(name, ravenMessage.sentByUserId)

            messageAdapter.setMessageText(text, ravenMessage.text, body)

            messageAdapter.setMessageTimeAndStatus(time, ravenMessage.timestamp)
            messageAdapter.setProperties(itemView, ravenMessage)
            messageAdapter.setListener(itemView, ravenMessage)
        }
    }

    private class ReceivedMessageWithImageViewHolder(val messageAdapter: MessageAdapter, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(ravenMessage: RavenMessage) {
            val image: ImageView = itemView.findViewById(R.id.image)
            val body: LinearLayout = itemView.findViewById(R.id.body)
            val name: TextView = itemView.findViewById(R.id.name)
            val text: TextView = itemView.findViewById(R.id.text)
            val time: TextView = itemView.findViewById(R.id.time)

            messageAdapter.setName(name, ravenMessage.sentByUserId)

            messageAdapter.setImage(image, ravenMessage.fileRef)

            if (ravenMessage.text != null) {
                body.visibility = View.VISIBLE
                text.visibility = View.VISIBLE
                messageAdapter.setMessageText(text, ravenMessage.text, body)
            } else {
                body.visibility = name.visibility
                text.visibility = View.GONE
            }

            messageAdapter.setMessageTimeAndStatus(time, ravenMessage.timestamp)
            messageAdapter.setProperties(itemView, ravenMessage)
            messageAdapter.setListener(itemView, ravenMessage)
        }
    }


    fun setProperties(itemView: View, ravenMessage: RavenMessage) {

        if (ravenMessage.selected)
            itemView.setBackgroundResource(R.color.colorMessageSelected)
        else
            itemView.setBackgroundResource(android.R.color.transparent)

        setTimeBanner(ravenMessage.messageId, itemView)
    }

    private fun setTimeBanner(messageId: String, itemView: View) {

        val timeText = timeBanner[messageId]

        val insertPoint = itemView.findViewById(R.id.root) as ViewGroup
        val banner = insertPoint.findViewById<View>(R.id.banner)

        if (timeText != null) {
            val text = DateTimeFormat.forPattern("MMMM dd, yyyy").print(DateTime.parse(timeText))
            if (banner != null) {
                banner.visibility = View.VISIBLE
                banner.banner.text = text
            } else {
                val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val bannerLayout = layoutInflater.inflate(R.layout.layout_message_banner, null)
                bannerLayout.banner.text = text
                insertPoint.addView(bannerLayout, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        } else if (banner != null) {
            banner.visibility = View.GONE
        }
    }

    fun setImage(imageView: ImageView, fileRef: String?) {

        imageView.setImageDrawable(context.getDrawable(R.drawable.hourglass))
        if (fileRef != null)
            FirebaseStorageUtil.getDownloadUrl(context, fileRef) { url ->
                GlideUtilV2.loadImageInChat(context, url, imageView)
            }
    }

    fun setName(nameTextView: TextView, userId: String?) {

        val ravenThread = ravenThreadResult.first(null)
        if (ravenThread != null && ravenThread.isGroup && userId != null) {

            val ravenUser = ravenThread.users?.findLast { ru ->
                ru.userId == userId
            }

            val name = ravenUser?.contactName ?: ravenUser?.displayName
            ?: ravenUser?.phoneNumber
            ?: "Raven User"


            nameTextView.visibility = View.VISIBLE
            nameTextView.text = name
            nameTextView.setTextColor(getColor(userId))
        } else
            nameTextView.visibility = View.GONE
    }

    private fun getColor(userId: String): Int {

        if (!colorsForUsers.containsKey(userId))
            colorsForUsers[userId] = getRandomColor()

        return colorsForUsers[userId] ?: getRandomColor()
    }

    private fun getRandomColor(): Int {

        val rand = Random()
        return Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
    }

    fun setMessageText(text: TextView, encryptedText: String?, body: LinearLayout? = null) {

        val colorStateList = Common.getColorStateList(Color.rgb(163, 163, 163))
        val ravenThread = ravenThreadResult.first(null)
        if (ravenThread != null) {
            try {
                setMessageTextUtil(text, body, ThreadManager.decryptMessage(ravenThread.threadId, encryptedText), Color.WHITE, Color.BLACK, if (body == null) null else Common.getColorStateList(Color.WHITE))
            } catch (e: GeneralSecurityException) {
                setMessageTextUtil(text, body, "Couldn't decrypt.", Color.WHITE, Color.WHITE, colorStateList)
            } catch (e: NullPointerException) {
                setMessageTextUtil(text, body, "Message Deleted.", Color.WHITE, Color.WHITE, colorStateList)
            } catch (e: Exception) {
                setMessageTextUtil(text, body, "There was a problem.", Color.WHITE, Color.WHITE, colorStateList)
            }
        } else
            setMessageTextUtil(text, body, " ", Color.WHITE, Color.BLACK, if (body == null) null else Common.getColorStateList(Color.WHITE))
    }

    private fun setMessageTextUtil(text: TextView, body: LinearLayout?, message: String, color1: Int, color2: Int, colorStateList: ColorStateList?) {

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
                    time.backgroundTintList = Common.getColorStateList(Color.rgb(113, 255, 146))
                } else {
                    time.setTextColor(Color.WHITE)
                    time.backgroundTintList = null
                }
            }

        } else
            time.visibility = View.GONE
    }

    fun setListener(itemView: View, ravenMessage: RavenMessage) {

        itemView.setOnClickListener {
            onClickListener.onClick(ravenMessage)
        }

        itemView.setOnLongClickListener {
            onClickListener.onLongClick(ravenMessage)
            true
        }
    }

    interface OnClickListener {
        fun onClick(ravenMessage: RavenMessage)
        fun onLongClick(ravenMessage: RavenMessage)
    }
}