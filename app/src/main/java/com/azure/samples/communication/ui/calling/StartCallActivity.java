package com.azure.samples.communication.ui.calling;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.microsoft.fluentui.widget.Button;

public class StartCallActivity extends AppCompatActivity {

    private EditText startCallDisplayName;
    private Button startCallNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_call);

        final ActionBar ab = getSupportActionBar();
        // Disable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Join");
        }

        startCallDisplayName = findViewById(R.id.start_call_display_name);
        startCallNextButton = findViewById(R.id.start_call_next_button);
        startCallNextButton.setOnClickListener(l -> goToMeetingInvitePage());
    }

    private void goToMeetingInvitePage() {
        Log.d("Mohtasim", "Start call next button clicked!!");
    }
}