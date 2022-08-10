package com.azure.samples.communication.ui.calling.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.views.activities.IntroViewActivity;
import com.microsoft.fluentui.widget.Button;

public class SignInActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(l -> navigateToIntroViewPage());
    }

    private void navigateToIntroViewPage() {
        View progressOverlay = findViewById(R.id.overlay_loading);
        progressOverlay.setVisibility(View.VISIBLE);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressOverlay.setVisibility(View.GONE);
                final Intent intent = new Intent(SignInActivity.this, IntroViewActivity.class);
                startActivity(intent);
            }
        }, 2000);
    }
}