// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall.helpers;
import android.content.Context;
import android.util.Log;
import com.android.volley.RequestQueue;
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
    private RequestQueue queue;
    private String acsTokenFetchUrl;
    private Callable<String> getAuthTokenFunction;

    public TokenService(Context applicationContext, String acsTokenFetchUrl, Callable<String> getAuthTokenFunction) {
        this.acsTokenFetchUrl = acsTokenFetchUrl;
        this.getAuthTokenFunction = getAuthTokenFunction;
        this.queue = Volley.newRequestQueue(applicationContext);
    }

    public CompletableFuture<String> getACSTokenAsync() {
        CompletableFuture<String> tokenCompletableFuture = new CompletableFuture<>();
        JsonObjectRequest request = new JsonObjectRequest(acsTokenFetchUrl, null, response -> {
            parseResponse(response, tokenCompletableFuture);
        }, error -> Log.d("error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String secureToken = null;
                try {
                    secureToken = getAuthTokenFunction.call();
                    Log.d(LOG_TAG, "Secure Token: " + secureToken);
                } catch (Exception e) {
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

    private void parseResponse(JSONObject response, CompletableFuture<String> tokenCompletableFuture) {
        String userToken;
        try {
            userToken = response.getString("token");
            tokenCompletableFuture.complete(userToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
