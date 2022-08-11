package com.azure.samples.communication.ui.calling.views.activities;

import static com.azure.samples.communication.ui.calling.contracts.Constants.DISPLAY_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.TextView;

import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.externals.authentication.AADAuthHandler;
import com.azure.samples.communication.ui.calling.externals.authentication.UserProfile;
import com.azure.samples.communication.ui.calling.utilities.AppSettings;
import com.azure.samples.communication.ui.calling.views.activities.IntroViewActivity;
import com.microsoft.fluentui.widget.Button;

public class SignInActivity extends AppCompatActivity {

    private static final String LOG_TAG = SignInActivity.class.getSimpleName();

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

    private void toggleProgress(boolean show){
        View progressOverlay = findViewById(R.id.overlay_loading);
        if (show){
            progressOverlay.setVisibility(View.VISIBLE);
        }
        else {
            progressOverlay.setVisibility(View.GONE);
        }
    }

    private void navigateToIntroViewPage() {
        if (appSettings.isAADAuthEnabled()) {
            toggleProgress(true);
            authHandler.loadAccount(this, (isAccountFound) -> {
                if (!isAccountFound) {
                  authHandler.signIn(this, () -> {
                      authHandler.callGraphAPI(this, (object) -> {
                          if(object instanceof UserProfile) {
                              //username.setText(((UserProfile) object).getDisplayName());
                              final Intent intent = new Intent(this, IntroViewActivity.class);
                              intent.putExtra(DISPLAY_NAME, ((UserProfile) object).getDisplayName());
                              toggleProgress(false);
                              startActivity(intent);
                          } else {
                              //Log.d(LOG_TAG, object.toString());
                              final Intent intent = new Intent(this, IntroViewActivity.class);
                              intent.putExtra(DISPLAY_NAME, "");
                              toggleProgress(false);
                              startActivity(intent);
                          }
                      });
                  });
                } else {
                    authHandler.callGraphAPI(this, (object) -> {
                        if(object instanceof UserProfile) {
                            //username.setText(((UserProfile) object).getDisplayName());
                            final Intent intent = new Intent(this, IntroViewActivity.class);
                            intent.putExtra(DISPLAY_NAME, ((UserProfile) object).getDisplayName());
                            toggleProgress(false);
                            startActivity(intent);
                        } else {
                            //Log.d(LOG_TAG, object.toString());
                            final Intent intent = new Intent(this, IntroViewActivity.class);
                            intent.putExtra(DISPLAY_NAME, "");
                            toggleProgress(false);
                            startActivity(intent);
                        }
                    });

                }
            });
        } else {
            final Intent intent = new Intent(this, IntroViewActivity.class);
            startActivity(intent);
        }
    }
}