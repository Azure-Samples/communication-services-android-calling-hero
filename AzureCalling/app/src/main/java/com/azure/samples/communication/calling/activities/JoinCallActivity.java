// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.azure.samples.communication.calling.helpers.Constants;
import com.azure.samples.communication.calling.helpers.JoinCallType;
import com.azure.samples.communication.calling.R;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class JoinCallActivity extends AppCompatActivity {
    private static final String LOG_TAG = JoinCallActivity.class.getSimpleName();
    private EditText groupCallEditText;
    private EditText teamsMeetingEditText;
    private Button joinButton;
    private RadioButton groupCallRadioButton;
    private RadioButton teamsMeetingRadioButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_call);

        // Get a support ActionBar corresponding to this toolbar
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Join a call");
        }

        initializeUI();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "JoinCallActivity - onDestroy");
        super.onDestroy();
    }

    private void initializeUI() {
        groupCallEditText = findViewById(R.id.group_call_text);
        teamsMeetingEditText = findViewById(R.id.teams_meeting_text);

        joinButton = findViewById(R.id.join_button);

        groupCallRadioButton = findViewById(R.id.group_call_radio_button);
        teamsMeetingRadioButton = findViewById(R.id.teams_meeting_radio_button);

        groupCallEditText.addTextChangedListener(onEditTextChanged());
        groupCallEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                groupCallRadioButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            } else {
                groupCallRadioButton.setTextColor(ContextCompat.getColor(this, R.color.textbox_secondary));
            }
        });


        teamsMeetingEditText.addTextChangedListener(onEditTextChanged());
        teamsMeetingEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                teamsMeetingRadioButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            } else {
                teamsMeetingRadioButton.setTextColor(ContextCompat.getColor(this, R.color.textbox_secondary));
            }
        });

        groupCallRadioButton.setOnClickListener(l -> {
            groupCallEditText.setVisibility(View.VISIBLE);
            groupCallEditText.requestFocus();
            if (teamsMeetingRadioButton.isChecked()) {
                teamsMeetingRadioButton.setChecked(false);
                teamsMeetingEditText.getText().clear();
                teamsMeetingEditText.setVisibility(View.GONE);
            }
        });

        teamsMeetingRadioButton.setOnClickListener(l -> {
            teamsMeetingEditText.setVisibility(View.VISIBLE);
            teamsMeetingEditText.requestFocus();
            if (groupCallRadioButton.isChecked()) {
                groupCallRadioButton.setChecked(false);
                groupCallEditText.getText().clear();
                groupCallEditText.setVisibility(View.GONE);
            }
        });
    }

    private TextWatcher onEditTextChanged() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    final CharSequence charSequence, final int i, final int i1, final int i2) {
                // Do nothing
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (editable.length() == 0) {
                    //TODO: Error: "SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length" occurs after
                    // clearing text
                    joinButton.setClickable(false);
                    joinButton.setEnabled(false);
                } else {
                    joinButton.setEnabled(true);
                    joinButton.setClickable(true);
                    if (!joinButton.hasOnClickListeners()) {
                        joinButton.setOnClickListener(l -> joinCall());
                    }
                }
            }
        };
    }

    private void joinCall() {
        Log.d(LOG_TAG, "Join call button clicked!");
        if (groupCallRadioButton.isChecked()) {
            final String joinId = groupCallEditText.getText().toString().trim();
            if (!isValidJoinId(joinId)) {
                showInvalidInputDialog();
            } else {
                final Intent intent = new Intent(this, SetupActivity.class);
                intent.putExtra(Constants.CALL_TYPE, JoinCallType.GROUP_CALL);
                intent.putExtra(Constants.JOIN_ID, joinId);
                startActivity(intent);
            }
        } else if (teamsMeetingRadioButton.isChecked()) {
            final String teamsMeetingUrl = teamsMeetingEditText.getText().toString().trim();
            if (!isValidTeamsMeetingUrl(teamsMeetingUrl)) {
                showInvalidInputDialog();
            } else {
                final Intent intent = new Intent(this, SetupActivity.class);
                intent.putExtra(Constants.CALL_TYPE, JoinCallType.TEAMS_MEETING);
                intent.putExtra(Constants.JOIN_ID, teamsMeetingUrl);
                startActivity(intent);
            }
        }
    }

    private boolean isValidTeamsMeetingUrl(final String teamsMeetingUrl) {
        try {
            new URL(teamsMeetingUrl).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException exception) {
            return false;
        }
    }

    private void showInvalidInputDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getErrorMessage())
                .setTitle("Unable to join")
                .setCancelable(false)
                .setPositiveButton("Dismiss", null);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private String getErrorMessage() {
        if (groupCallRadioButton.isChecked()) {
            return "The meeting ID entered is invalid. Please try again.";
        }
        return "The meeting link entered is invalid. Please try again.";
    }

    private boolean isValidJoinId(final String joinId) {
        try {
            return UUID.fromString(joinId).toString().equals(joinId);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
