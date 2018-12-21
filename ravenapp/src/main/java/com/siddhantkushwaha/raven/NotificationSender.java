package com.siddhantkushwaha.raven;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationSender {

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

    public void sendNotificationWithReplyAction(String userId, String threadId, String replyLabel) {

        PendingIntent activityPendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.drawable.logo_raven_silhoutte)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(activityPendingIntent)
                .setAutoCancel(true);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            RemoteInput remoteInput = new RemoteInput.Builder(context.getString(R.string.key_notification_reply))
                    .setLabel(replyLabel).build();


            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra(context.getString(R.string.key_thread_id), threadId);
            intent.putExtra(context.getString(R.string.key_user_id), userId);
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.button_send_message, replyLabel, replyPendingIntent)
                    .addRemoteInput(remoteInput).build();

            builder.addAction(action);
        }

        Notification newMessageNotification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Log.i(getClass().toString(), notificationId);
        notificationManager.notify(notificationId, 0, newMessageNotification);
    }
}
