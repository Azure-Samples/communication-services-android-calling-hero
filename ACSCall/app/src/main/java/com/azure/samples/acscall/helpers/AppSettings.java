// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall.helpers;

import android.content.Context;
import java.io.IOException;
import java.util.Properties;

public final class AppSettings {
    private static Properties properties;
    private static final String CONFIG_FILE = "appSettings.properties";
    private static final String TOKEN_URL = "acsTokenFetchUrl";
    private static final String IS_AAD_AUTH_ENABLED = "isAADAuthEnabled";
    private static final String SCOPES = "aadScopes";

    public AppSettings(Context context) {
        try {
            properties = new Properties();
            properties.load(context.getAssets().open(CONFIG_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getACSTokenFetchUrl() {
        return properties.getProperty(TOKEN_URL);
    }

    public String[] getAADScopes() {
        return properties.getProperty(SCOPES).split(",");
    }

    public boolean isAADAuthEnabled() {
        String isAadAuthEnabled = properties.getProperty(IS_AAD_AUTH_ENABLED);
        return Boolean.parseBoolean(isAadAuthEnabled);
    }
}
