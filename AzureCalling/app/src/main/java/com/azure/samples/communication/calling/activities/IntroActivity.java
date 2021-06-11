// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.azure.samples.communication.calling.external.authentication.AADAuthHandler;
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.helpers.AppSettings;
import com.azure.samples.communication.calling.helpers.Constants;
import com.azure.samples.communication.calling.helpers.JoinCallType;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class IntroActivity extends AppCompatActivity {
    private static final String LOG_TAG = IntroActivity.class.getSimpleName();

    private Button startButton;
    private Button joinButton;
    private Button signInButton;
    private TextView signOutTextView;
    private AADAuthHandler authHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }
        
        setContentView(R.layout.activity_intro);

        // Get a support ActionBar corresponding to this toolbar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initializeUI();
        initializeAuth();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "IntroActivity - onDestroy");
        super.onDestroy();
    }

    private void initializeUI() {
        startButton = findViewById(R.id.intro_start_button);
        startButton.setOnClickListener(l -> setupCall());

        joinButton = findViewById(R.id.intro_join);
        joinButton.setOnClickListener(l -> joinCall());

        signInButton = findViewById(R.id.intro_sign_in);
        signInButton.setOnClickListener(l -> signIn());

        signOutTextView = findViewById(R.id.intro_sign_out);
        signOutTextView.setOnClickListener(l -> signOut());

        final TextView learnTextView = findViewById(R.id.intro_learn);
        learnTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initializeAuth() {
        authHandler = ((AzureCalling) getApplication()).getAadAuthHandler();
        // if Azure Active directory authentication is enabled, display Login UI
        final AppSettings appSettings = ((AzureCalling) getApplication()).getAppSettings();
        if (appSettings.isAADAuthEnabled()) {
            hideAllButtons();
            authHandler.loadAccount(this, (isAccountFound) -> {
                if (isAccountFound) {
                    userSignedIn();
                } else {
                    userSignedOut();
                }
            });
        }
    }

    private void hideAllButtons() {
        hideCallButtons();
        hideSignInButton();
        hideSignOutButton();
    }

    private void userSignedIn() {
        showCallButtons();
        hideSignInButton();
        showSignOutButton();
    }

    private void userSignedOut() {
        showSignInButton();
        hideCallButtons();
        hideSignOutButton();
    }

    private void signIn() {
        authHandler.signIn(this, () -> {
            userSignedIn();
        });
    }

    private void signOut() {
        authHandler.signOut(() -> {
            userSignedOut();
        });
    }

    private void setupCall() {
        Log.d(LOG_TAG, "Setup new meeting button clicked!");
        ((AzureCalling) getApplication()).createCallingContext();
        final Intent intent = new Intent(this, SetupActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.CALL_TYPE, JoinCallType.GROUP_CALL);
        startActivity(intent);
    }

    private void joinCall() {
        Log.d(LOG_TAG, "Join call button clicked!");
        ((AzureCalling) getApplication()).createCallingContext();
        final Intent intent = new Intent(this, JoinCallActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showSignInButton() {
        signInButton.setVisibility(View.VISIBLE);
    }

    private void hideSignInButton() {
        signInButton.setVisibility(View.GONE);
    }

    private void showSignOutButton() {
        signOutTextView.setVisibility(View.VISIBLE);
    }

    private void hideSignOutButton() {
        signOutTextView.setVisibility(View.GONE);
    }

    private void hideCallButtons() {
        startButton.setVisibility(View.GONE);
        joinButton.setVisibility(View.GONE);
    }

    private void showCallButtons() {
        startButton.setVisibility(View.VISIBLE);
        joinButton.setVisibility(View.VISIBLE);
    }
}
