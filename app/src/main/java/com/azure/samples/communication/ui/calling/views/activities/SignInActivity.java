package com.azure.samples.communication.ui.calling.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.views.activities.IntroViewActivity;
import com.microsoft.fluentui.widget.Button;

public class SignInActivity extends AppCompatActivity {

    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(l -> navigateToIntroViewPage());
    }

    private void navigateToIntroViewPage() {
        final Intent intent = new Intent(this, IntroViewActivity.class);
        startActivity(intent);
    }
}