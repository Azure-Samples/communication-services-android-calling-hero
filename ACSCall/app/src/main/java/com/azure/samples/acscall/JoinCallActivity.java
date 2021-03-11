// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall;

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
import androidx.appcompat.app.AppCompatActivity;

public class JoinCallActivity extends AppCompatActivity {
    private static final String LOG_TAG = JoinCallActivity.class.getSimpleName();
    private EditText editTextTextMeetingName;
    private Button joinButton;
    private TextView joinButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_call);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Join a call");
        }

        initializeUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        editTextTextMeetingName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // Do nothing
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 0) {
                        //TODO: Error: "SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length" occurs after
                        // clearing text
                        joinButton.setClickable(false);
                        joinButton.setEnabled(false);
                        joinButtonText.setEnabled(false);
                    } else {
                        joinButton.setEnabled(true);
                        joinButtonText.setEnabled(true);
                        joinButton.setClickable(true);
                        if (!joinButton.hasOnClickListeners()) {
                            joinButton.setOnClickListener(l -> joinCall());
                        }
                    }
                }
            });

        joinButton = findViewById(R.id.join_button);
        joinButtonText = findViewById(R.id.join_button_text);
    }

    private void joinCall() {
        Log.d(LOG_TAG, "Join call button clicked!");
        Intent intent = new Intent(this, SetupActivity.class);
        intent.putExtra(Constants.GROUP_ID, editTextTextMeetingName.getText().toString());
        startActivity(intent);
    }
}
