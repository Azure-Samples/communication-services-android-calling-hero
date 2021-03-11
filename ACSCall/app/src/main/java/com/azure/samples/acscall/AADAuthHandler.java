// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.samples.acscall.helpers.AppSettings;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import java.util.function.Consumer;

public class AADAuthHandler {
    private static final String LOG_TAG = AADAuthHandler.class.getSimpleName();
    private AppSettings appSettings;
    private ISingleAccountPublicClientApplication mSingleAccountApp;
    private String accessToken;

    public AADAuthHandler(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public void signIn(Activity activity, Runnable onSuccess) {
        if (mSingleAccountApp == null) {
            return;
        }
        mSingleAccountApp.signIn(activity, null, appSettings.getAADScopes(), new AuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                accessToken = authenticationResult.getAccessToken();
                onSuccess.run();
            }

            @Override
            public void onError(MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "User cancelled login.");
            }
        });
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void signOut(Runnable onSuccess) {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
            @Override
            public void onSignOut() {
                onSuccess.run();
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
        });
    }

    private void getCurrentAccount(Activity activity, Consumer<Boolean> aadCallback) {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(@Nullable IAccount activeAccount) {
                if (activeAccount == null) {
                    aadCallback.accept(false);
                } else {
                    mSingleAccountApp.acquireToken(activity, appSettings.getAADScopes(), new AuthenticationCallback() {

                        @Override
                        public void onSuccess(IAuthenticationResult authenticationResult) {
                            accessToken = authenticationResult.getAccessToken();
                            aadCallback.accept(true);
                        }

                        @Override
                        public void onError(MsalException exception) {
                            Log.e(LOG_TAG, exception.getMessage());
                        }

                        @Override
                        public void onCancel() {
                            Log.d(LOG_TAG, "User cancelled login.");
                        }
                    });
                }
            }

            @Override
            public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    aadCallback.accept(false);
                }
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
        });
    }


    //When app comes to the foreground, load existing account to determine if user is signed in
    public void loadAccount(Activity activity, Consumer<Boolean> aadCallback) {
        PublicClientApplication.createSingleAccountPublicClientApplication(activity.getApplicationContext(),
                R.raw.auth_config_single_account,
                new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(ISingleAccountPublicClientApplication application) {
                        mSingleAccountApp = application;
                        getCurrentAccount(activity, aadCallback);
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.e(LOG_TAG, exception.getMessage());
                    }
                });

    }
}
