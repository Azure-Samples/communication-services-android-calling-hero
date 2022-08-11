package com.azure.samples.communication.ui.calling.utilities;

import android.content.Context;

import java.io.IOException;
import java.util.Properties;

public final class AppSettings {
    private static final String CONFIG_FILE = "Azure.properties";
    private static final String TOKEN_URL = "communicationTokenFetchUrl";
    private static final String IS_AAD_AUTH_ENABLED = "isAADAuthEnabled";
    private static final String SCOPES = "aadScopes";
    private static final String GRAPH_URL = "graphURL";

    private final Properties properties;

    public AppSettings(final Context context) {
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
}
