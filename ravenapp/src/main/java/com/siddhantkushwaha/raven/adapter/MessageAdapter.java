package com.siddhantkushwaha.raven.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.localEntity.RavenMessage;
import com.siddhantkushwaha.raven.manager.ThreadManager;
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.utility.GlideUtilV2;
import com.siddhantkushwaha.raven.utility.JodaTimeUtilV2;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.security.GeneralSecurityException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class MessageAdapter extends RealmRecyclerViewAdapter {

    private Context context;
    private OrderedRealmCollection<RavenMessage> data;

    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    private FirebaseStorageUtil firebaseStorageUtil;

    public MessageAdapter(Context context, @Nullable OrderedRealmCollection data, boolean autoUpdate) {
        super(data, autoUpdate);

        this.context = context;
        this.data = data;

        this.firebaseStorageUtil = new FirebaseStorageUtil();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = null;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_message_sent, viewGroup, false);
                return new MessageAdapter.SentMessageHolder(view);
            case 2:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_message_received, viewGroup, false);
                return new MessageAdapter.ReceivedMessageHolder(view);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getMessageType(FirebaseAuth.getInstance().getUid());
    }

    @Nullable
    @Override
    public RavenMessage getItem(int index) {
        return data.get(index);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        RavenMessage ravenMessage = data.get(position);

        boolean showDate = true;
        int previousMessageType = -1;
        if (position > 0) {

            RavenMessage previousRavenMessage = data.get(position - 1);

            String currentMessageTime = ravenMessage.getTimestamp();
            if (currentMessageTime == null) currentMessageTime = ravenMessage.getLocalTimestamp();

            String previousMessageTime = previousRavenMessage.getTimestamp();
            if (previousMessageTime == null)
                previousMessageTime = previousRavenMessage.getLocalTimestamp();

            if (JodaTimeUtilV2.dateCmp(DateTime.parse(currentMessageTime), DateTime.parse(previousMessageTime)) == 0)
                showDate = false;

            previousMessageType = this.getItemViewType(position - 1);
        }

        switch (holder.getItemViewType()) {
            case 1:
                ((MessageAdapter.SentMessageHolder) holder).bind(ravenMessage, previousMessageType, showDate, position);
                break;
            case 2:
                ((MessageAdapter.ReceivedMessageHolder) holder).bind(ravenMessage, previousMessageType, showDate, position);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        View view;
        LinearLayout bannerLayout;
        TextView bannerText;
        ImageView image;
        TextView messageText;
        TextView timeText;
        LinearLayout messageBody;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            view = itemView;
            bannerLayout = itemView.findViewById(R.id.bannerLayout);
            bannerText = itemView.findViewById(R.id.bannerText);
            image = itemView.findViewById(R.id.image);
            messageText = itemView.findViewById(R.id.text);
            timeText = itemView.findViewById(R.id.sent_time);
            messageBody = itemView.findViewById(R.id.messageBody);
        }

        void bind(RavenMessage ravenMessage, int previousMessageType, boolean showDate, final int position) {

            makeBanner(showDate, bannerLayout, bannerText, ravenMessage);

            setMessageContent(image, messageText, ravenMessage);

            setMessageTime(timeText, ravenMessage);

            setMessageProperties(ravenMessage, messageBody);

            setListener(view, position);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        View view;
        LinearLayout bannerLayout;
        TextView bannerText;
        ImageView image;
        TextView messageText;
        TextView timeText;
        ImageView status;
        LinearLayout messageBody;

        SentMessageHolder(View itemView) {
            super(itemView);
            view = itemView;
            bannerLayout = itemView.findViewById(R.id.bannerLayout);
            bannerText = itemView.findViewById(R.id.bannerText);
            image = itemView.findViewById(R.id.image);
            messageText = itemView.findViewById(R.id.text);
            timeText = itemView.findViewById(R.id.sent_time);
            status = itemView.findViewById(R.id.status);
            messageBody = itemView.findViewById(R.id.messageBody);
        }

        void bind(RavenMessage ravenMessage, int previousMessageType, boolean showDate, final int position) {

            makeBanner(showDate, bannerLayout, bannerText, ravenMessage);

            setMessageContent(image, messageText, ravenMessage);

            setMessageTime(timeText, ravenMessage);

            if (ravenMessage.getSeenAt() != null)
                status.setBackground(context.getDrawable(R.drawable.badge_message_status_seen));
            else if (ravenMessage.getTimestamp() != null)
                status.setBackground(context.getDrawable(R.drawable.badge_message_status_sent));
            else
                status.setBackground(context.getDrawable(R.drawable.badge_message_status_pending));

            setMessageProperties(ravenMessage, messageBody);

            setListener(view, position);
        }
    }


    public interface OnClickListener {
        void onClickListener(View view, int position);
    }

    public interface OnLongClickListener {
        void onLongClickListener(View view, int position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    // Utility functions begin from here .........

    private void makeBanner(boolean showDate, LinearLayout bannerLayout, TextView bannerText, RavenMessage ravenMessage) {

        if (showDate) {

            bannerLayout.setVisibility(View.VISIBLE);
            bannerText.setVisibility(View.VISIBLE);

            String messageTime = ravenMessage.getTimestamp();
            if (messageTime == null)
                messageTime = ravenMessage.getLocalTimestamp();

            bannerText.setText(DateTimeFormat.forPattern("MMMM dd, yyyy").print(DateTime.parse(messageTime)));

        } else {
            bannerLayout.setVisibility(View.GONE);
            bannerText.setVisibility(View.GONE);
        }
    }

    private void setMessageContent(ImageView imageView, TextView messageText, RavenMessage ravenMessage) {

        String text = ravenMessage.getText();
        String fileRef = ravenMessage.getFileRef();

        if (text == null && fileRef == null) {

            messageText.setVisibility(View.VISIBLE);
            setErrorView(messageText, "Message Deleted.");

            imageView.setVisibility(View.GONE);

        } else if (text == null) {

            messageText.setVisibility(View.GONE);
            setDefaultView(messageText, ravenMessage, null);

            imageView.setVisibility(View.VISIBLE);

            // download media
            loadImage(imageView, fileRef);

        } else if (fileRef == null) {

            messageText.setVisibility(View.VISIBLE);
            setMessageText(messageText, ravenMessage);

            imageView.setVisibility(View.GONE);
        } else {

            messageText.setVisibility(View.VISIBLE);
            setMessageText(messageText, ravenMessage);

            imageView.setVisibility(View.VISIBLE);

            // download media
            loadImage(imageView, fileRef);
        }
    }

    private void setMessageText(TextView messageText, RavenMessage ravenMessage) {
        try {
            setDefaultView(messageText, ravenMessage, ThreadManager.decryptMessage(ravenMessage.getThreadId(), ravenMessage.getText()));
        } catch (GeneralSecurityException e) {
            setErrorView(messageText, "Couldn't Decrypt");
        } catch (Exception e) {
            setErrorView(messageText, "There was a problem.");
        }
    }

    private void setMessageTime(TextView timeText, RavenMessage ravenMessage) {
        if (ravenMessage.getTimestamp() != null) {

            timeText.setVisibility(View.VISIBLE);

            DateTime time = DateTime.parse(ravenMessage.getTimestamp());
            DateTimeFormatter build = DateTimeFormat.forPattern("hh:mm a");
            timeText.setText(build.print(time));
        } else {
            timeText.setVisibility(View.GONE);
        }
    }

    private void setMessageProperties(RavenMessage ravenMessage, LinearLayout messageBody) {

        if (ravenMessage.getSelected())
            messageBody.setBackgroundResource(R.color.colorMessageSelected);
        else
            messageBody.setBackgroundResource(android.R.color.transparent);
    }

    private void setListener(View view, Integer position) {
        view.setOnClickListener(v -> {
            if (onClickListener != null)
                onClickListener.onClickListener(v, position);
        });

        view.setOnLongClickListener(v -> {
            if (onLongClickListener != null)
                onLongClickListener.onLongClickListener(v, position);
            return false;
        });
    }

    private void setDefaultView(TextView messageText, RavenMessage ravenMessage, String decryptedMessaged) {

        messageText.setText(decryptedMessaged);
        switch (ravenMessage.getMessageType(FirebaseAuth.getInstance().getUid())) {
            case 1:
                messageText.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                messageText.setBackground(context.getDrawable(R.drawable.background_message_holder_indigo));
                break;
            case 2:
                messageText.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                messageText.setBackground(context.getDrawable(R.drawable.background_message_holder_white));
                break;
        }
    }

    private void setErrorView(TextView messageText, String errorMessage) {
        messageText.setText(errorMessage);
        messageText.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        messageText.setBackground(context.getDrawable(R.drawable.background_message_holder_red));
    }

    private void loadImage(ImageView imageView, String fileRef) {
        firebaseStorageUtil.getDownloadUrl(context, fileRef, uri -> {

            GlideUtilV2.loadImageInChat(context, uri, imageView);
        });
    }
}
