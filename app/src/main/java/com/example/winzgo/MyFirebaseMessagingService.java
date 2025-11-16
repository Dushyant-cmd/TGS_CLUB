package com.example.winzgo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.winzgo.activities.Splash;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String TAG = "FirebaseService.java";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getNotification());
        if (remoteMessage.getNotification() != null) {
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else {
            showNotification("Someone answered on your question.", "click here");
        }
    }

    @Override
    public void handleIntent(Intent intent) {
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        try {
            if (title.isEmpty())
                title = "You have a new win";
            else if (body.isEmpty())
                body = "click here";
            String channelId = getPackageName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "notification",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("description notification");

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

            Intent i = new Intent(MyFirebaseMessagingService.this, Splash.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this,
                    101, i, PendingIntent.FLAG_IMMUTABLE |
                            PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelId);
            notification.setContentTitle(title);
            notification.setContentText(body);
            notification.setAutoCancel(true);
            notification.setPriority(Notification.PRIORITY_HIGH);
            notification.setSmallIcon(R.drawable.tsc_logo);
            notification.setContentIntent(pendingIntent);

            NotificationManagerCompat compat = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                compat.notify(59, notification.build());
            }
        } catch (Exception e) {
            Log.d(TAG, "showNotification: " + e);
        }
    }

    /**
     * Below method will be called by Firebase to android then android call this method
     * with new Firebase cloud messaging token.
     *
     * @param token it is fcm token
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "onNewToken: " + token);
    }
}