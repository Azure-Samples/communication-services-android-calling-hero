package com.azure.samples.communication.ui.calling.externals.authentication;

import static com.azure.samples.communication.ui.calling.contracts.Constants.DISPLAY_NAME;
import static com.azure.samples.communication.ui.calling.contracts.Constants.EMAIL;
import static com.azure.samples.communication.ui.calling.contracts.Constants.GIVEN_NAME;
import static com.azure.samples.communication.ui.calling.contracts.Constants.ID;
import static com.azure.samples.communication.ui.calling.contracts.Constants.SURNAME;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.utilities.AppSettings;
import com.azure.samples.communication.ui.calling.utilities.MSGraphRequestWrapper;
import com.microsoft.identity.client.AcquireTokenSilentParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Consumer;

public class AADAuthHandler {

    private static final String LOG_TAG = AADAuthHandler.class.getSimpleName();

    private final AppSettings appSettings;
    private ISingleAccountPublicClientApplication mSingleAccountApp;
    private String accessToken = null;
    private String[] mScopes = { "User.Read" };

    public AADAuthHandler(final AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public void signIn(final Activity activity, final Runnable onSuccess) {
        if (mSingleAccountApp == null) {
            return;
        }
        mSingleAccountApp.signIn(activity, null, mScopes, new AuthenticationCallback() {

            @Override
            public void onSuccess(final IAuthenticationResult authenticationResult) {
                accessToken = authenticationResult.getAccessToken();
                onSuccess.run();
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

    private void findUserProfile(Activity activity, final Consumer<Object> authCallback) {
        MSGraphRequestWrapper.callGraphAPIUsingVolley(
                activity,
                appSettings.getGraphUrl() + "/me",
                accessToken,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            UserProfile userProfile = new UserProfile();
                            userProfile.setDisplayName(response.get(DISPLAY_NAME).toString());
                            userProfile.setGivenName(response.get(GIVEN_NAME).toString());
                            userProfile.setSurname(response.get(SURNAME).toString());
                            userProfile.setEmail(response.get(EMAIL).toString());
                            userProfile.setId(response.get(ID).toString());

                            Log.d(LOG_TAG, response.toString());
                            authCallback.accept(userProfile);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(LOG_TAG, error.getMessage());
                        authCallback.accept(error);
                    }
                }
        );
    }

    public void callGraphAPI(Activity activity, final Consumer<Object> authCallback) {

        if(accessToken == null || accessToken.length() == 0) {
            acquireToken(activity, (token) -> {
                accessToken = token;
                findUserProfile(activity, authCallback);
            });
        }  else {
            findUserProfile(activity, authCallback);
        }

    }

    public String getAccessToken() {
        return accessToken;
    }

    public void signOut(final Runnable onSuccess) {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
            @Override
            public void onSignOut() {
                onSuccess.run();
            }

            @Override
            public void onError(@NonNull final MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
        });
    }

    private void acquireToken(final Activity activity, final Consumer<String> callback) {
        mSingleAccountApp.acquireToken(activity, appSettings.getAADScopes(), new AuthenticationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(final IAuthenticationResult authenticationResult) {
                accessToken = authenticationResult.getAccessToken();
                callback.accept(accessToken);
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

    private void getCurrentAccount(final Activity activity, final Consumer<Boolean> aadCallback) {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onAccountLoaded(@Nullable final IAccount activeAccount) {
                if (activeAccount == null) {
                    aadCallback.accept(false);
                } else {
                    acquireToken(activity, (token) -> {
                        if(token != null && token.length() > 0) aadCallback.accept(true);
                    });
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onAccountChanged(
                    @Nullable final IAccount priorAccount,
                    @Nullable final IAccount currentAccount) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    aadCallback.accept(false);
                }
            }

            @Override
            public void onError(@NonNull final MsalException exception) {
                Log.e(LOG_TAG, exception.getMessage());
            }
        });
    }


    //When app comes to the foreground, load existing account to determine if user is signed in
    public void loadAccount(final Activity activity, final Consumer<Boolean> aadCallback) {
        PublicClientApplication.createSingleAccountPublicClientApplication(activity.getApplicationContext(),
                R.raw.auth_config_single_account,
                new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(final ISingleAccountPublicClientApplication application) {
                        mSingleAccountApp = application;
                        getCurrentAccount(activity, aadCallback);
                    }

                    @Override
                    public void onError(final MsalException exception) {
                        Log.e(LOG_TAG, exception.getMessage());
                    }
                });
    }
}
