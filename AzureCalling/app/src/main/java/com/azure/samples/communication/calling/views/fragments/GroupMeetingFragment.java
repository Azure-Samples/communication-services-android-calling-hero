// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.models.CallCompositeMultitaskingOptions;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.contracts.Constants;
import com.azure.samples.communication.calling.contracts.SampleErrorMessages;
import com.azure.samples.communication.calling.externals.calling.CallingContext;
import com.azure.samples.communication.calling.utilities.AppSettings;
import com.microsoft.fluentui.widget.Button;

import java.util.UUID;

/**
 * Fragment to host group call meetings
 */
public class GroupMeetingFragment extends AbstractBaseFragment {

    private EditText groupMeetingID;
    private EditText displayNameEditor;
    private AppSettings appSettings;

    public GroupMeetingFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        appSettings = ((AzureCalling) requireActivity().getApplicationContext()).getAppSettings();
        final View inflatedView = inflater.inflate(R.layout.fragment_group_meeting, container, false);
        final Button joinCallButton = inflatedView.findViewById(R.id.group_call_join_next);
        joinCallButton.setOnClickListener(l -> joinCall());

        displayNameEditor = inflatedView.findViewById(R.id.group_call_display_name);
        final String savedDisplayName = appSettings.getUserProfile().getDisplayName();
        if (!TextUtils.isEmpty(savedDisplayName)) {
            displayNameEditor.setText(savedDisplayName);
        }
        groupMeetingID = inflatedView.findViewById(R.id.group_call_id);
        final String savedGroupCallID = getSharedPreferences().getString(Constants.ACS_GROUPCALL_ID, "");
        if (!TextUtils.isEmpty(savedGroupCallID)) {
            groupMeetingID.setText(savedGroupCallID);
        }
        return inflatedView;
    }

    private void joinCall() {
        final String displayName = displayNameEditor.getText().toString();
        final String groupCallId = groupMeetingID.getText().toString().trim();
        groupMeetingID.setText(groupCallId);
        appSettings.getUserProfile().setDisplayName(displayName);

        if (TextUtils.isEmpty(groupCallId)) {
            showError(SampleErrorMessages.GROUP_ID_REQUIRED);
            return;
        }
        if (!isUUID(groupCallId)) {
            showError(SampleErrorMessages.GROUP_ID_INVALID);
            return;
        }

        if (TextUtils.isEmpty(displayName)) {
            showError(SampleErrorMessages.DISPLAY_NAME_REQUIRED);
            return;
        }
        getSharedPreferences()
                .edit()
                .putString(Constants.ACS_GROUPCALL_ID, groupCallId)
                .apply();

        if (appSettings.getUserProfile().getUsername().isEmpty()) {
            appSettings.getUserProfile().setUsername(displayName);
        }

        final CallComposite composite = new CallCompositeBuilder()
                .multitasking(new CallCompositeMultitaskingOptions(true, true))
                .build();

        final AzureCalling calling = (AzureCalling) requireActivity().getApplicationContext();
        calling.createCallingContext();
        final CallingContext callingContext = calling.getCallingContext();

        composite.addOnErrorEventHandler(callCompositeEventHandler);
        final CallCompositeRemoteOptions remoteOptions = callingContext
                .getCallCompositeGroupRemoteOptions(displayName, UUID.fromString(groupCallId));
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
