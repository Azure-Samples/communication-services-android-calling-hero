package com.azure.samples.communication.ui.calling.views.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.CallCompositeEventHandler;
import com.azure.android.communication.ui.calling.models.CallCompositeErrorCode;
import com.azure.android.communication.ui.calling.models.CallCompositeErrorEvent;
import com.azure.android.communication.ui.calling.models.CallCompositeGroupCallLocator;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.contracts.Constants;
import com.azure.samples.communication.ui.calling.contracts.SampleErrorMessages;
import com.azure.samples.communication.ui.calling.externals.calling.CallingContext;
import com.microsoft.fluentui.widget.Button;

import java.util.UUID;

/**
 * Fragment to host group call meetings
 */
public class GroupMeetingFragment extends AbstractBaseFragment {

    private EditText groupMeetingID;
    private EditText displayNameEditor;

    public GroupMeetingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_group_meeting, container, false);
        Button joinCallButton = inflatedView.findViewById(R.id.group_call_join_next);
        joinCallButton.setOnClickListener(l -> joinCall());

        displayNameEditor = inflatedView.findViewById(R.id.group_call_display_name);
        final String savedDisplayName = getSharedPreferences().getString(Constants.ACS_DISPLAY_NAME, null);
        if(!TextUtils.isEmpty(savedDisplayName)) {
            displayNameEditor.setText(savedDisplayName);
        }
        groupMeetingID = inflatedView.findViewById(R.id.group_call_id);
        final String savedGroupCallID = getSharedPreferences().getString(Constants.ACS_GROUPCALL_ID, null);
        if (!TextUtils.isEmpty(savedGroupCallID)){
            groupMeetingID.setText(savedGroupCallID);
        }
        return inflatedView;
    }

    private void joinCall() {
        final String displayName = displayNameEditor.getText().toString();
        final String groupCallId = groupMeetingID.getText().toString().trim();
        groupMeetingID.setText(groupCallId);
        if (TextUtils.isEmpty(groupCallId)){
            showError(SampleErrorMessages.GROUP_ID_REQUIRED);
            return;
        }
        if (!isUUID(groupCallId)){
            showError(SampleErrorMessages.GROUP_ID_INVALID);
            return ;
        }
        if (TextUtils.isEmpty(displayName)){
            showError(SampleErrorMessages.DISPLAY_NAME_REQUIRED);
            return ;
        }
        getSharedPreferences()
                .edit()
                .putString(Constants.ACS_DISPLAY_NAME, displayName)
                .putString(Constants.ACS_GROUPCALL_ID, groupCallId)
                .apply();

        final CallComposite composite = new CallCompositeBuilder()
                .build();

        AzureUICalling calling = (AzureUICalling) requireActivity().getApplicationContext();
        calling.createCallingContext();
        CallingContext callingContext = calling.getCallingContext();
        CallCompositeRemoteOptions remoteOptions = new CallCompositeRemoteOptions(
                new CallCompositeGroupCallLocator(UUID.fromString(groupCallId)),
                callingContext.getCommunicationTokenCredential(),
                displayName
                );
        composite.addOnErrorEventHandler(callCompositeEventHandler);
        composite.launch(requireActivity(), remoteOptions);
    }

    private boolean isUUID(final String groupCallID) {
        try {
            UUID.fromString(groupCallID);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}