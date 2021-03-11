// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class EndActivity extends AppCompatActivity {

    TextView endTextView;
    Button feedBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        feedBackButton = findViewById(R.id.end_feedback_button);
        feedBackButton.setOnClickListener(l -> {
            Uri uri = Uri.parse(getString(R.string.end_feedbackUrl));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        endTextView = findViewById(R.id.end_return);
        endTextView.setOnClickListener(l -> finish());
    }
}
