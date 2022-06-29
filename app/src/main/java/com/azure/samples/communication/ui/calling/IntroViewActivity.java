package com.azure.samples.communication.ui.calling;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

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
    }

    private void startCall() {
        Log.d("Mohtasim", "StartCall button clicked!");
    }

    private void joinCall() {
        Log.d("Mohtasim", "JoinCall button clicked!");

    }
}