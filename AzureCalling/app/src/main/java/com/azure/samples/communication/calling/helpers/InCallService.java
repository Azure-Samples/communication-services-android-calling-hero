// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

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

import com.azure.samples.communication.calling.activities.CallActivity;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.external.calling.TokenService;

public class InCallService extends Service {
    private static final String LOG_TAG = TokenService.class.getSimpleName();
    private static final String IN_CALL_CHANNEL_ID = "IN_CALL";

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(LOG_TAG, "InCallService onStartCommand called");
        createInCallNotificationChannel(this);
        startInCallNotification(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        Log.d(LOG_TAG, "InCallService onTaskRemoved called");
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "InCallService onCreate called");
        super.onCreate();
    }

    public void createInCallNotificationChannel(final Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final CharSequence name = "AzureCalling Call Status";
            final String description = "Provides a notification for on-going calls";
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel(IN_CALL_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            final NotificationManager notificationManager
                    = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startInCallNotification(final Context context) {
        final Intent intent = new Intent(context, CallActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        final int notificationId = 100;

        final Notification notification =
            new NotificationCompat.Builder(context, IN_CALL_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_fluent_call_16_filled)
                    .setContentTitle("AzureCalling")
                    .setContentText("Return to your call")
                    .setContentIntent(pendingIntent)
                    .setSound(null)
                    .build();

        startForeground(notificationId, notification);
    }
}
