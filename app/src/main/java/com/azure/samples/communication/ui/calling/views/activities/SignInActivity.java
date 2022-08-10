package com.azure.samples.communication.ui.calling.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.externals.authentication.AADAuthHandler;
import com.azure.samples.communication.ui.calling.utilities.AppSettings;
import com.azure.samples.communication.ui.calling.views.activities.IntroViewActivity;
import com.microsoft.fluentui.widget.Button;

public class SignInActivity extends AppCompatActivity {

    private Button signInButton;
    private AADAuthHandler authHandler;
    private AppSettings appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);



        initializeAuth();
        initializeUI();
    }

    private void initializeUI() {
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(l -> navigateToIntroViewPage());
    }

    private void initializeAuth() {
        authHandler = ((AzureUICalling) getApplication()).getAadAuthHandler();
        appSettings = ((AzureUICalling) getApplication()).getAppSettings();
    }

    private void navigateToIntroViewPage() {
        if (appSettings.isAADAuthEnabled()) {
            View progressOverlay = findViewById(R.id.overlay_loading);
            progressOverlay.setVisibility(View.VISIBLE);
            authHandler.loadAccount(this, (isAccountFound) -> {
                progressOverlay.setVisibility(View.GONE);
                if (!isAccountFound) {
                  authHandler.signIn(this, () -> {
                      Log.d("Mohtasim", "Signed in");
                      final Intent intent = new Intent(this, IntroViewActivity.class);
                      startActivity(intent);
                  });
                } else {
                    Log.d("Mohtasim", "Signed in");
                    final Intent intent = new Intent(this, IntroViewActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}