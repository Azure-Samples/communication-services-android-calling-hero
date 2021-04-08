// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.azure.samples.communication.calling.R;

public class EndActivity extends AppCompatActivity {
    private static final String LOG_TAG = EndActivity.class.getSimpleName();
    private TextView endTextView;
    private Button feedBackButton;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startIntroActivity();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "EndActivity - onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        feedBackButton = findViewById(R.id.end_feedback_button);
        feedBackButton.setOnClickListener(l -> {
            final Uri uri = Uri.parse(getString(R.string.end_feedbackUrl));
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        endTextView = findViewById(R.id.end_return);
        endTextView.setOnClickListener(l -> {
            startIntroActivity();
            finish();
        });
    }

    private void startIntroActivity() {
        final Intent intent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

