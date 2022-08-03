package com.azure.samples.communication.ui.calling.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.models.CallCompositeJoinLocator;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.contracts.CallType;
import com.microsoft.fluentui.widget.Button;

public class InvitationActivity extends AppCompatActivity {

    private Button startCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        startCallButton = findViewById(R.id.start_call_continue_button);
        startCallButton.setOnClickListener(l -> makeCall());

    }

    private void makeCall() {
        Log.d("Mohtasim", "continue...");
        ((AzureUICalling) getApplicationContext()).createCallingContext();
        final CallCompositeRemoteOptions options = ((AzureUICalling) getApplicationContext())
                .getCallingContext()
                .getCallCompositeRemoteOptions(CallType.GROUP_CALL);

        final CallComposite composite = new CallCompositeBuilder().build();
        composite.launch(this, options);
    }


}