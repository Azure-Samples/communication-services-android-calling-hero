// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall.helpers;

import android.content.Context;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public final class TokenService {
    private static final String LOG_TAG = TokenService.class.getSimpleName();

    private final RequestQueue queue;
    private final String acsTokenFetchUrl;
    private final Callable<String> getAuthTokenFunction;

    public TokenService(
            final Context applicationContext,
            final String acsTokenFetchUrl,
            final Callable<String> getAuthTokenFunction) {
        this.acsTokenFetchUrl = acsTokenFetchUrl;
        this.getAuthTokenFunction = getAuthTokenFunction;
        this.queue = Volley.newRequestQueue(applicationContext);
    }

    public CompletableFuture<String> getACSTokenAsync() {
        final CompletableFuture<String> tokenCompletableFuture = new CompletableFuture<>();
        final Response.Listener<JSONObject> responseListener = response ->
                parseResponse(response, tokenCompletableFuture);
        final Response.ErrorListener errorListener = error -> Log.e(LOG_TAG, "Failed getting ACS token", error);
        final JsonObjectRequest request = new JsonObjectRequest(
                acsTokenFetchUrl, null, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                final HashMap<String, String> headers = new HashMap<>();
                String secureToken = null;
                try {
                    secureToken = getAuthTokenFunction.call();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                if (secureToken != null) {
                    headers.put("Authorization", "Bearer " + secureToken);
                }
                return headers;
            }
        };
        this.queue.add(request);

        return tokenCompletableFuture;
    }

    private void parseResponse(final JSONObject response, final CompletableFuture<String> tokenCompletableFuture) {
        try {
            final String userToken = response.getString("token");
            tokenCompletableFuture.complete(userToken);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }
}
