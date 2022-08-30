// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.activities;

import static com.azure.samples.communication.calling.contracts.Constants.IS_LOGGED_IN;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.contracts.Constants;
import com.azure.samples.communication.calling.externals.authentication.AADAuthHandler;
import com.azure.samples.communication.calling.externals.authentication.UserProfile;
import com.azure.samples.communication.calling.utilities.AppSettings;
import com.microsoft.fluentui.persona.AvatarView;
import com.microsoft.fluentui.widget.Button;

public class IntroViewActivity extends AppCompatActivity {

    private static final String LOG_TAG = IntroViewActivity.class.getSimpleName();

    private TextView usernameTextView;
    private AvatarView avatarView;
    private Button signOutButton;
    private Button startCallButton;
    private Button joinCallButton;

    private AADAuthHandler authHandler;
    private AppSettings appSettings;

    private SharedPreferences sharedPreferences;

    private String username;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_view);

        authHandler = ((AzureCalling) getApplication()).getAadAuthHandler();
        appSettings = ((AzureCalling) getApplication()).getAppSettings();


        sharedPreferences = this.getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        final boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

        if (appSettings.isAADAuthEnabled()) {
            if (!isLoggedIn) {
                sharedPreferences.edit().clear().apply();
                final Intent intent = new Intent(this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (isLoggedIn && appSettings.getAuthenticationToken().getToken() == null) {
                final View progressOverlay = findViewById(R.id.intro_overlay_loading);
                progressOverlay.setVisibility(View.VISIBLE);
                authHandler.loadAccount(this, isLoggedIn, (profile) -> {
                    if (profile instanceof UserProfile) {
                        ((TextView) findViewById(R.id.username_textview))
                                .setText(((UserProfile) profile).getUsername());
                        ((AvatarView) findViewById(R.id.avatar_view))
                                .setName(((UserProfile) profile).getUsername());
                        ((AvatarView) findViewById(R.id.avatar_view)).invalidate();
                    }
                    progressOverlay.setVisibility(View.GONE);
                });
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeUI();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initializeUI() {

        username = appSettings.getUserProfile().getUsername();

        startCallButton = findViewById(R.id.start_call_button);
        startCallButton.setOnClickListener(l -> startCall());

        joinCallButton = findViewById(R.id.join_call_button);
        joinCallButton.setOnClickListener(l -> joinCall());

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.view_intro_actionbar);
        }

        signOutButton = findViewById(R.id.signout_button);
        if (!appSettings.isAADAuthEnabled()) {
            signOutButton.setText(getString(R.string.sign_in));
        }
        signOutButton.setOnClickListener(l -> signOut());

        usernameTextView = findViewById(R.id.username_textview);
        usernameTextView.setText(username);

        avatarView = findViewById(R.id.avatar_view);
        avatarView.setName(username);
    }

    private void signOut() {

        if (!appSettings.isAADAuthEnabled()) {
            return;
        }
        usernameTextView.setText("");
        avatarView.setName("");

        authHandler.signOut(this, (isSignedOut) -> {
            sharedPreferences.edit().clear().apply();
            final Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void startCall() {
        final Intent intent = new Intent(this, StartCallActivity.class);
        startActivity(intent);
    }

    private void joinCall() {
        final Intent intent = new Intent(this, JoinCallActivity.class);
        startActivity(intent);
    }
}
