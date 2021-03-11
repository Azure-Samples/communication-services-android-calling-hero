// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.azure.samples.acscall.CallActivity;
import com.azure.samples.acscall.R;

public class InCallService extends Service {
    private static final String LOG_TAG = TokenService.class.getSimpleName();
    private static final String IN_CALL_CHANNEL_ID = "IN_CALL";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "InCallService onStartCommand called");
        createInCallNotificationChannel(this);
        startInCallNotification(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG, "InCallService onTaskRemoved called");
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "InCallService onCreate called");
        super.onCreate();
    }

    public void createInCallNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ACSCall Call Status";
            String description = "Provides a notification for on-going calls";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(IN_CALL_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            NotificationManager notificationManager
                    = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startInCallNotification(Context context) {
        Intent intent = new Intent(context, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        int notificationId = 100;

        Notification notification =
            new NotificationCompat.Builder(context, IN_CALL_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_fluent_call_16_filled)
                    .setContentTitle("ACSCall")
                    .setContentText("Return to your call")
                    .setContentIntent(pendingIntent)
                    .setSound(null)
                    .build();

        startForeground(notificationId, notification);
    }
}
