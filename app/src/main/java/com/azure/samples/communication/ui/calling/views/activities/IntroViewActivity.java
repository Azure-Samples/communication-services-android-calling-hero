package com.azure.samples.communication.ui.calling.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.azure.samples.communication.ui.calling.R;
import com.microsoft.fluentui.widget.Button;

public class IntroViewActivity extends AppCompatActivity {

    private Button startCallButton;
    private Button joinCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_view);

        startCallButton = findViewById(R.id.start_call_button);
        startCallButton.setOnClickListener(l -> startCall());

        joinCallButton = findViewById(R.id.join_call_button);
        joinCallButton.setOnClickListener(l -> joinCall());
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.view_intro_actionbar);
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