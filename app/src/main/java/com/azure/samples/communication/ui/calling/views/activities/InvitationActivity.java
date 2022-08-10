package com.azure.samples.communication.ui.calling.views.activities;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.models.CallCompositeJoinLocator;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.contracts.CallType;
import com.azure.samples.communication.ui.calling.externals.calling.CallingContext;
import com.microsoft.fluentui.widget.Button;

public class InvitationActivity extends AppCompatActivity {

    private Button startCallButton;
    private Button shareCallButton;

    private CallingContext callingContext;
    private CallCompositeRemoteOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

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
        ((AzureUICalling) getApplicationContext()).createCallingContext();
        callingContext = ((AzureUICalling) getApplicationContext()).getCallingContext();

        options = ((AzureUICalling) getApplicationContext())
                .getCallingContext()
                .getCallCompositeRemoteOptions(CallType.GROUP_CALL);
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