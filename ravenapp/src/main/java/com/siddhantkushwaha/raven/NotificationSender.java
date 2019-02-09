package com.siddhantkushwaha.raven;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.siddhantkushwaha.android.thugtools.thugtools.utility.GlideApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationSender {

    private static final String REPLY_LABEL = "REPLY";

    private Context context;

    private Integer requestCode;

    private String channelId;
    private String channelName;

    private Intent intent;
    private String notificationId;
    private String contentTitle;
    private String contentText;

    public NotificationSender(Context context, String notificationId, Integer requestCode, String contentTitle, String contentText, Intent intent) {

        this.context = context;
        this.intent = intent;

        this.channelId = this.context.getString(R.string.default_channel_id);
        this.channelName = this.context.getString(R.string.default_channel_name);

        this.requestCode = requestCode;
        this.notificationId = notificationId;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
    }

    public static void cancelNotification(Context context, String notificationId, Integer id) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId, id);
        }
    }

    public void sendNotificationWithReplyAction(String userId, String threadId, String picUrl) {

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.layout_new_message_notification);
        notificationLayout.setTextViewText(R.id.title, contentTitle);
        notificationLayout.setTextViewText(R.id.message, contentText);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.image_unknown_user_circle);
        requestOptions.placeholder(R.drawable.image_unknown_user_circle);

        Log.i(NotificationSender.class.toString(), "HERE6");
        GlideApp.with(context)
                .asBitmap()
                .load(picUrl)
                .circleCrop()
                .apply(requestOptions)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        notificationLayout.setImageViewBitmap(R.id.displayPic, resource);
                        buildAndSend(notificationLayout, userId, threadId);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);

                        buildAndSend(notificationLayout, userId, threadId);
                    }
                });
    }

    private void buildAndSend(RemoteViews notificationLayout, String userId, String threadId) {

        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(requestCode, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.drawable.logo_raven_silhoutte)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            RemoteInput remoteInput = new RemoteInput.Builder(NotificationReceiver.NOTIFICATION_REPLY)
                    .setLabel(REPLY_LABEL).build();

            Intent intent = NotificationReceiver.getIntent(context, userId, threadId);
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.button_send_message, REPLY_LABEL, replyPendingIntent)
                    .addRemoteInput(remoteInput).build();

            builder.addAction(action);
        }

        Notification newMessageNotification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(notificationId, 0, newMessageNotification);
    }
}
