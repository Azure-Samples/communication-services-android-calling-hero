package com.azure.samples.communication.ui.calling.views.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.azure.samples.communication.ui.calling.R;
import com.microsoft.fluentui.widget.Button;

public class StartCallActivity extends AppCompatActivity {

    private EditText startCallDisplayName;
    private Button startCallNextButton;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_call);

        final ActionBar ab = getSupportActionBar();
        // Disable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Start Call");
        }

        initialize();
    }

    private void initialize() {
        sharedPreferences = getSharedPreferences(Constants.ACS_DISPLAY_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        startCallDisplayName = findViewById(R.id.start_call_display_name);
        startCallNextButton = findViewById(R.id.start_call_next_button);
        startCallNextButton.setOnClickListener(l -> goToMeetingInvitePage());

        final String savedDisplayName = sharedPreferences.getString(Constants.ACS_DISPLAY_NAME, "");
        if(savedDisplayName.length() > 0) {
            startCallDisplayName.setText(savedDisplayName);
        }
    }

    private void goToMeetingInvitePage() {
        final Intent intent = new Intent(this, InvitationActivity.class);
        editor.putString(Constants.ACS_DISPLAY_NAME, startCallDisplayName.getText().toString());
        editor.commit();
        startActivity(intent);
    }
}