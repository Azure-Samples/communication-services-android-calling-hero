// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.activities.CallActivity;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.external.calling.TokenService;

public class InCallService extends Service {
    private static final String LOG_TAG = TokenService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(LOG_TAG, "InCallService onStartCommand called");
        startInCallNotification(this);
        return START_NOT_STICKY;
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

    public void startInCallNotification(final Context context) {
        final Intent intent = new Intent(context, CallActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        final int notificationId = 100;

        final Notification notification =
            new NotificationCompat.Builder(context, AzureCalling.IN_CALL_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_fluent_call_16_filled)
                    .setContentTitle("AzureCalling")
                    .setContentText("Return to your call")
                    .setContentIntent(pendingIntent)
                    .setSound(null)
                    .build();

        startForeground(notificationId, notification);
    }
}
