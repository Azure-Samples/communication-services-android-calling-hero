// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.activities;

import static com.azure.samples.communication.calling.contracts.Constants.START_CALL;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.contracts.Constants;
import com.microsoft.fluentui.widget.Button;

public class StartCallActivity extends AppCompatActivity {

    private EditText startCallDisplayName;
    private Button startCallNextButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_call);

        final ActionBar ab = getSupportActionBar();
        // Disable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(START_CALL);
        }

        initializeUI();
    }

    private void initializeUI() {
        sharedPreferences = getSharedPreferences(Constants.ACS_SHARED_PREF, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        startCallDisplayName = findViewById(R.id.start_call_display_name);
        startCallNextButton = findViewById(R.id.start_call_next_button);
        startCallNextButton.setOnClickListener(l -> goToMeetingInvitePage());

        final String savedDisplayName = sharedPreferences.getString(Constants.ACS_DISPLAY_NAME, "");
        if (savedDisplayName.length() > 0) {
            startCallDisplayName.setText(savedDisplayName);
        }
    }

    private void goToMeetingInvitePage() {

        final String displayName = startCallDisplayName.getText().toString();
        if (TextUtils.isEmpty(displayName)) {
            // Throw exception
            return;
        }

        final Intent intent = new Intent(this, InvitationActivity.class);
        editor.putString(Constants.ACS_DISPLAY_NAME, startCallDisplayName.getText().toString());
        editor.commit();
        startActivity(intent);
    }
}
