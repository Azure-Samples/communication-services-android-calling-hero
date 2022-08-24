// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling;

import android.app.Application;
import com.azure.samples.communication.calling.externals.authentication.AADAuthHandler;
import com.azure.samples.communication.calling.utilities.AppSettings;
import com.azure.samples.communication.calling.externals.calling.CallingContext;
import com.azure.samples.communication.calling.externals.calling.TokenService;

public class AzureCalling extends Application {

    public static final String IN_CALL_CHANNEL_ID = "IN_CALL";

    private AppSettings appSettings;
    private CallingContext callingContext;
    private AADAuthHandler aadAuthHandler;
    private TokenService tokenService;

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
    }

    public void createCallingContext() {
        this.callingContext = new CallingContext(getApplicationContext(),
                () -> tokenService.getCommunicationTokenAsync().get());
    }

    public CallingContext getCallingContext() {
        return this.callingContext;
    }

    public AADAuthHandler getAadAuthHandler() {
        return aadAuthHandler;
    }

    public AppSettings getAppSettings() {
        return appSettings;
    }

}
