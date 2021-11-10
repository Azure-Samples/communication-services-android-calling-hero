// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.azure.samples.communication.calling.external.authentication.AADAuthHandler;
import com.azure.samples.communication.calling.external.calling.CallingContext;
import com.azure.samples.communication.calling.helpers.AudioSessionManager;
import com.azure.samples.communication.calling.helpers.PermissionHelper;
import com.azure.samples.communication.calling.external.calling.TokenService;
import com.azure.samples.communication.calling.helpers.AppSettings;

public class AzureCalling extends Application {

    public static final String IN_CALL_CHANNEL_ID = "IN_CALL";

    private AppSettings appSettings;
    private CallingContext callingContext;
    private AADAuthHandler aadAuthHandler;
    private TokenService tokenService;
    private PermissionHelper permissionHelper;
    private AudioSessionManager audioSessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeDependencies();
        createInCallNotificationChannel();
    }

    private void initializeDependencies() {
        this.appSettings = new AppSettings(getApplicationContext());
        final String tokenGenerationAddress = appSettings.getCommunicationToken();
        this.aadAuthHandler = new AADAuthHandler(appSettings);
        this.tokenService = new TokenService(
                getApplicationContext(), tokenGenerationAddress, () -> aadAuthHandler.getAccessToken());
        this.permissionHelper = new PermissionHelper();
    }

    public void createCallingContext() {
        this.callingContext = new CallingContext(getApplicationContext(),
            () -> tokenService.getCommunicationTokenAsync().get());
    }

    public void createAudioSessionManager() {
        this.audioSessionManager = new AudioSessionManager(
                (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
    }

    public CallingContext getCallingContext() {
        return callingContext;
    }

    public AADAuthHandler getAadAuthHandler() {
        return aadAuthHandler;
    }

    public AppSettings getAppSettings() {
        return appSettings;
    }

    public PermissionHelper getPermissionHelper() {
        return permissionHelper;
    }

    public AudioSessionManager getAudioSessionManager() {
        return audioSessionManager;
    }

    private void createInCallNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final CharSequence name = "AzureCalling Call Status";
            final String description = "Provides a notification for on-going calls";
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel(IN_CALL_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
