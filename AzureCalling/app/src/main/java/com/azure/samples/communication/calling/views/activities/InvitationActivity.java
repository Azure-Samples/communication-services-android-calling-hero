// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.activities;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import static com.azure.samples.communication.calling.contracts.Constants.ACS_DISPLAY_NAME;
import static com.azure.samples.communication.calling.contracts.Constants.INVITE_ANOTHER_DEVICE;
import static com.azure.samples.communication.calling.contracts.Constants.JOIN_CALL;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.contracts.Constants;
import com.azure.samples.communication.calling.externals.calling.CallingContext;
import com.microsoft.fluentui.widget.Button;

public class InvitationActivity extends AppCompatActivity {

    private Button startCallButton;
    private Button shareCallButton;

    private CallingContext callingContext;
    private CallCompositeRemoteOptions options;
    private SharedPreferences sharedPreferences;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        final ActionBar ab = getSupportActionBar();
        // Disable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(INVITE_ANOTHER_DEVICE);
        }

        sharedPreferences = this.getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        startCallSetup();
        initializeUI();
    }

    private void initializeUI() {
        startCallButton = findViewById(R.id.start_call_continue_button);
        startCallButton.setOnClickListener(l -> makeCall());

        shareCallButton = findViewById(R.id.share_button);
        shareCallButton.setOnClickListener(l -> openShareDialogue());
    }

    private void startCallSetup() {
        ((AzureCalling) getApplicationContext()).createCallingContext();
        callingContext = ((AzureCalling) getApplicationContext()).getCallingContext();

        options = ((AzureCalling) getApplicationContext())
                .getCallingContext()
                .getCallCompositeRemoteOptions(sharedPreferences.getString(ACS_DISPLAY_NAME, ""));
    }

    private void openShareDialogue() {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, callingContext.getJoinId());
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Group Call ID");
        sendIntent.setType("text/plain");
        final Intent shareIntent = Intent.createChooser(sendIntent, null);
        shareIntent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(shareIntent);
    }

    private void makeCall() {
        final CallComposite composite = new CallCompositeBuilder().build();
        composite.launch(this, options);
    }


}