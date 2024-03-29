// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.activities;

import static com.azure.samples.communication.calling.contracts.Constants.IS_LOGGED_IN;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.contracts.Constants;
import com.azure.samples.communication.calling.externals.authentication.AADAuthHandler;
import com.azure.samples.communication.calling.externals.authentication.UserProfile;
import com.azure.samples.communication.calling.utilities.AppSettings;
import com.microsoft.fluentui.widget.Button;

public class SignInActivity extends AppCompatActivity {

    private static final String LOG_TAG = SignInActivity.class.getSimpleName();

    private Button signInButton;
    private AADAuthHandler authHandler;
    private AppSettings appSettings;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initializeAuth();
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!appSettings.isAADAuthEnabled()) {
            navigateToIntroView();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void initializeUI() {
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(l -> onClickSignInButton());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initializeAuth() {
        sharedPreferences = this.getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        authHandler = ((AzureCalling) getApplication()).getAadAuthHandler();
        appSettings = ((AzureCalling) getApplication()).getAppSettings();
    }

    private void toggleProgress(final boolean show) {
        final View progressOverlay = findViewById(R.id.overlay_loading);
        if (show) {
            progressOverlay.setVisibility(View.VISIBLE);
            signInButton.setText(R.string.signing_in);
            signInButton.setEnabled(false);
        } else {
            progressOverlay.setVisibility(View.GONE);
            signInButton.setText(R.string.sign_in);
            signInButton.setEnabled(true);
        }
    }

    private void navigateToIntroView() {
        final Intent intent = new Intent(this, IntroViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        toggleProgress(false);
        startActivity(intent);
    }

    private void cacheSignIn() {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.apply();
    }

    private void onClickSignInButton() {
        if (appSettings.isAADAuthEnabled()) {
            toggleProgress(true);

            authHandler.loadAccount(this, false, (object) -> {
                if (object instanceof Boolean) {
                    authHandler.signIn(this, (profile) -> {
                        if (profile instanceof UserProfile) {
                            cacheSignIn();
                            navigateToIntroView();
                        }
                    });
                }
                if (object instanceof UserProfile) {
                    cacheSignIn();
                    navigateToIntroView();
                }
            });
        }
    }
}
