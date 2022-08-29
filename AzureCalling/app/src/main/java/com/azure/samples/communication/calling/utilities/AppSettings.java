// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.utilities;

import android.content.Context;

import com.azure.samples.communication.calling.externals.authentication.AuthenticationToken;
import com.azure.samples.communication.calling.externals.authentication.UserProfile;

import java.io.IOException;
import java.util.Properties;

public final class AppSettings {
    private static final String CONFIG_FILE = "appSettings.properties";
    private static final String TOKEN_URL = "communicationTokenFetchUrl";
    private static final String IS_AAD_AUTH_ENABLED = "isAADAuthEnabled";
    private static final String SCOPES = "aadScopes";
    private static final String GRAPH_URL = "graphURL";
    private static final String TENANT_ID = "tenant";
    private static Context context;


    private AuthenticationToken authenticationToken;
    private UserProfile userProfile;

    private final Properties properties;

    public AppSettings(final Context context) {
        this.context = context;
        authenticationToken = new AuthenticationToken();
        userProfile = new UserProfile();
        try {
            properties = new Properties();
            properties.load(context.getAssets().open(CONFIG_FILE));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCommunicationToken() {
        return properties.getProperty(TOKEN_URL);
    }

    public String[] getAADScopes() {
        return properties.getProperty(SCOPES).split(",");
    }

    public boolean isAADAuthEnabled() {
        final String isAadAuthEnabled = properties.getProperty(IS_AAD_AUTH_ENABLED);
        return Boolean.parseBoolean(isAadAuthEnabled);
    }

    public String getGraphUrl() {
        return properties.getProperty(GRAPH_URL);
    }

    public String getTenantId() {
        return (properties.getProperty(TENANT_ID) == null) ? "" : properties.getProperty(TENANT_ID);
    }

    public Context getContext() {
        return this.context;
    }

    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }
}
