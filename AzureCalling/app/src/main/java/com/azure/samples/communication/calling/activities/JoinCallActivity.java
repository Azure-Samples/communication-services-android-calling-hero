// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.azure.samples.communication.calling.helpers.Constants;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.helpers.JoinCallType;

import java.util.UUID;

public class JoinCallActivity extends AppCompatActivity {
    private static final String LOG_TAG = JoinCallActivity.class.getSimpleName();
    private EditText editTextTextMeetingName;
    private Button joinButton;
    private TextView joinEnterText;

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
        editTextTextMeetingName = findViewById(R.id.join_meeting);
        joinEnterText = findViewById(R.id.join_enter);
        editTextTextMeetingName.addTextChangedListener(new TextWatcher() {
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
            });
        editTextTextMeetingName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                joinEnterText.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            } else {
                joinEnterText.setTextColor(ContextCompat.getColor(this, R.color.textbox_secondary));
            }
        });
        joinButton = findViewById(R.id.join_button);
    }

    private void joinCall() {
        Log.d(LOG_TAG, "Join call button clicked!");
        final String joinId = editTextTextMeetingName.getText().toString().trim();
        if (!isValidJoinId(joinId)) {
            showInvalidJoinIdDialog();
        } else {
            final Intent intent = new Intent(this, SetupActivity.class);
            intent.putExtra(Constants.CALL_TYPE, JoinCallType.GROUP_CALL);
            intent.putExtra(Constants.JOIN_ID, joinId);
            startActivity(intent);
        }
    }

    private void showInvalidJoinIdDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The meeting ID entered is invalid. Please try again.")
                .setTitle("Unable to join")
                .setCancelable(false)
                .setPositiveButton("Dismiss", null);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isValidJoinId(final String joinId) {
        try {
            return UUID.fromString(joinId).toString().equals(joinId);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
