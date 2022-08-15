package com.azure.samples.communication.ui.calling.views.activities;

import static com.azure.samples.communication.ui.calling.contracts.Constants.ACS_DISPLAY_NAME;
import static com.azure.samples.communication.ui.calling.contracts.Constants.IS_LOGGED_IN;
import static com.azure.samples.communication.ui.calling.contracts.Constants.USERNAME;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.contracts.Constants;
import com.azure.samples.communication.ui.calling.externals.authentication.AADAuthHandler;
import com.azure.samples.communication.ui.calling.utilities.AppSettings;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_view);

        authHandler = ((AzureUICalling) getApplication()).getAadAuthHandler();
        appSettings = ((AzureUICalling) getApplication()).getAppSettings();

        sharedPreferences = this.getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        final boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

        if(appSettings.isAADAuthEnabled() && !isLoggedIn) {
            sharedPreferences.edit().clear().apply();
            final Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        initializeUI();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initializeUI() {

        username = sharedPreferences.getString(USERNAME, "");

        startCallButton = findViewById(R.id.start_call_button);
        startCallButton.setOnClickListener(l -> startCall());

        joinCallButton = findViewById(R.id.join_call_button);
        joinCallButton.setOnClickListener(l -> joinCall());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.view_intro_actionbar);
        }

        signOutButton = findViewById(R.id.signout_button);
        if(!appSettings.isAADAuthEnabled()) {
            signOutButton.setText(getString(R.string.sign_in));
        }
        signOutButton.setOnClickListener(l -> signOut());

        usernameTextView = findViewById(R.id.username_textview);
        usernameTextView.setText(username);

        avatarView = findViewById(R.id.avatar_view);
        avatarView.setName(username);
    }

    private void signOut() {
        final String buttonState = signOutButton.getText().toString();

        if(getString(R.string.sign_out).equals(buttonState)) {

            usernameTextView.setText("");
            avatarView.setName("");
            signOutButton.setText(getString(R.string.sign_in));

            sharedPreferences.edit().remove(ACS_DISPLAY_NAME).apply();
            sharedPreferences.edit().remove(IS_LOGGED_IN).apply();

            authHandler.signOut(() -> {});
        } else {
            final Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
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