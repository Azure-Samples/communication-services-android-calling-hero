// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.externals.authentication;

import static com.azure.samples.communication.calling.contracts.Constants.DISPLAY_NAME;
import static com.azure.samples.communication.calling.contracts.Constants.GIVEN_NAME;
import static com.azure.samples.communication.calling.contracts.Constants.ID;
import static com.azure.samples.communication.calling.contracts.SampleErrorMessages.USER_LOGIN_CANCEL;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.utilities.AppSettings;
import com.azure.samples.communication.calling.utilities.MSGraphRequestWrapper;
import com.microsoft.identity.client.AcquireTokenSilentParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AzureCloudInstance;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.function.Consumer;

public class AADAuthHandler {

    private static final String LOG_TAG = AADAuthHandler.class.getSimpleName();

    private final AppSettings appSettings;
    private ISingleAccountPublicClientApplication mSingleAccountApp;
    private String accessToken = null;

    public AADAuthHandler(final AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public void signIn(final Activity activity, final Consumer<Object> callback) {
        if (mSingleAccountApp == null) {
            return;
        }
        mSingleAccountApp.signIn(activity, null, appSettings.getAADScopes(), new AuthenticationCallback() {

            @Override
            public void onSuccess(final IAuthenticationResult authenticationResult) {
                accessToken = authenticationResult.getAccessToken();
                appSettings.getAuthenticationToken().setToken(accessToken);
                callGraphAPI(activity, (object) -> {
                    callback.accept(object);
                });
            }

            @Override
            public void onError(final MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, USER_LOGIN_CANCEL);
            }
        });
    }

    private void findUserProfile(final Activity activity, final Consumer<Object> authCallback) {

        MSGraphRequestWrapper.callGraphAPIUsingVolley(
                activity,
                appSettings.getGraphUrl() + "/me",
                appSettings.getAuthenticationToken().getToken(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        try {
                            final UserProfile userProfile = appSettings.getUserProfile();
                            userProfile.setDisplayName(response.get(DISPLAY_NAME).toString());
                            userProfile.setUsername(response.get(DISPLAY_NAME).toString());
                            userProfile.setGivenName(response.get(GIVEN_NAME).toString());
                            userProfile.setId(response.get(ID).toString());

                            authCallback.accept(userProfile);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        //Log.e(LOG_TAG, error.getMessage());
                        authCallback.accept(error);
                    }
                }
        );
    }

    public void callGraphAPI(final Activity activity, final Consumer<Object> authCallback) {

        if (accessToken == null || accessToken.length() == 0) {
            findUserProfile(activity, authCallback);
        }  else {
            findUserProfile(activity, authCallback);
        }
    }

    public String getAccessToken() {
        return appSettings.getAuthenticationToken().getToken();
    }

    private void signingOut(final Consumer<Boolean> signoutCallback) {
        mSingleAccountApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
            @Override
            public void onSignOut() {
                signoutCallback.accept(true);
            }

            @Override
            public void onError(@NonNull final MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
                signoutCallback.accept(false);
            }
        });
    }

    public void signOut(final Activity activity, final Consumer<Boolean> signOutCallback) {
        if (mSingleAccountApp == null) {
            signOutCallback.accept(false);
        } else {
            signingOut(signOutCallback);
        }
    }

    private void acquireTokenSilently(final Activity activity,
                                      final IAccount account,
                                      final Consumer<Object> callback) {

        mSingleAccountApp.acquireTokenSilentAsync(
                    new AcquireTokenSilentParameters(
                        new AcquireTokenSilentParameters
                                .Builder()
                                .withScopes(Arrays.asList(appSettings.getAADScopes()))
                                .fromAuthority(AzureCloudInstance.AzurePublic,
                                        appSettings.getTenantId())
                                .forAccount(account)
                                .withCallback(new SilentAuthenticationCallback() {
                                    @Override
                                    public void onSuccess(final IAuthenticationResult authenticationResult) {
                                        accessToken = authenticationResult.getAccessToken();
                                        appSettings.getAuthenticationToken().setToken(accessToken);
                                        callGraphAPI(activity, (object) -> {
                                            callback.accept(object);
                                        });
                                    }

                                    @Override
                                    public void onError(final MsalException exception) {
                                        acquireToken(activity, (object) -> {
                                            callback.accept(object);
                                        });
                                        Log.e(LOG_TAG, exception.getMessage());
                                    }
                                }).self()));
    }

    private void acquireToken(final Activity activity, final Consumer<Object> callback) {

        mSingleAccountApp.acquireToken(activity, appSettings.getAADScopes(), new AuthenticationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(final IAuthenticationResult authenticationResult) {
                accessToken = authenticationResult.getAccessToken();
                appSettings.getAuthenticationToken().setToken(accessToken);
                callGraphAPI(activity, callback);
            }

            @Override
            public void onError(final MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "User cancelled login.");
            }
        });
    }

    private void getCurrentAccount(final Activity activity,
                                   final boolean isLoggedIn,
                                   final Consumer<Object> aadCallback) {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onAccountLoaded(@Nullable final IAccount activeAccount) {
                if (activeAccount == null) {
                    aadCallback.accept(new Boolean(false));
                } else {
                    if (isLoggedIn) {
                        acquireTokenSilently(activity, activeAccount, (object) -> {
                            aadCallback.accept(object);
                        });
                    } else {
                        acquireToken(activity, (object) -> {
                            aadCallback.accept(object);
                        });
                    }
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onAccountChanged(
                    @Nullable final IAccount priorAccount,
                    @Nullable final IAccount currentAccount) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    aadCallback.accept(new Boolean(false));
                }
            }

            @Override
            public void onError(@NonNull final MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
        });
    }


    //When app comes to the foreground, load existing account to determine if user is signed in
    public void loadAccount(final Activity activity, final boolean isLoggedIn, final Consumer<Object> aadCallback) {
        PublicClientApplication.createSingleAccountPublicClientApplication(activity.getApplicationContext(),
                R.raw.auth_config_single_account,
                new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(final ISingleAccountPublicClientApplication application) {
                        mSingleAccountApp = application;
                        getCurrentAccount(activity, isLoggedIn, aadCallback);
                    }

                    @Override
                    public void onError(final MsalException exception) {
                        Log.e(LOG_TAG, exception.getMessage());
                    }
                });
    }
}
