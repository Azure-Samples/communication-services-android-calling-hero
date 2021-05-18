// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;

import com.azure.samples.communication.calling.external.authentication.AADAuthHandler;
import com.azure.samples.communication.calling.external.calling.CallingContext;
import com.azure.samples.communication.calling.helpers.AudioSessionManager;
import com.azure.samples.communication.calling.helpers.PermissionHelper;
import com.azure.samples.communication.calling.external.calling.TokenService;
import com.azure.samples.communication.calling.helpers.AppSettings;

public class AzureCalling extends Application {
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
}
