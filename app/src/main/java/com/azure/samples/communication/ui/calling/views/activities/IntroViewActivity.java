package com.azure.samples.communication.ui.calling.views.activities;

import static com.azure.samples.communication.ui.calling.contracts.Constants.DISPLAY_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.externals.authentication.AADAuthHandler;
import com.microsoft.fluentui.widget.Button;

public class IntroViewActivity extends AppCompatActivity {

    private static final String LOG_TAG = IntroViewActivity.class.getSimpleName();

    private Button startCallButton;
    private Button joinCallButton;
    private Button signOutButton;
    private TextView usernameTextView;

    private AADAuthHandler authHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_view);

        authHandler = ((AzureUICalling) getApplication()).getAadAuthHandler();

        Intent intent = getIntent();
        final String username = intent.getStringExtra(DISPLAY_NAME);

        startCallButton = findViewById(R.id.start_call_button);
        startCallButton.setOnClickListener(l -> startCall());

        Button joinCallButton = findViewById(R.id.join_call_button);
        joinCallButton.setOnClickListener(l -> joinCall());

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.view_intro_actionbar);

        signOutButton = findViewById(R.id.signout_button);
        signOutButton.setOnClickListener(l -> signOut());

        usernameTextView = findViewById(R.id.username_textview);
        usernameTextView.setText(username);

        Log.d(LOG_TAG, "username " + intent.getStringExtra(DISPLAY_NAME));
    }

    private void signOut() {
        authHandler.signOut(() -> {
            final Intent intent = new Intent(this, SignInActivity.class);
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