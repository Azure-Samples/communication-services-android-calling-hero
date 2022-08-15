package com.azure.samples.communication.ui.calling.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.models.CallCompositeGroupCallLocator;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.contracts.CallType;
import com.azure.samples.communication.ui.calling.contracts.Constants;
import com.azure.samples.communication.ui.calling.externals.calling.CallingContext;
import com.microsoft.fluentui.widget.Button;
import com.azure.samples.communication.ui.calling.AzureUICalling;

import java.util.UUID;

/**
 * Fragment to host group call meetings
 */
public class GroupMeetingFragment extends Fragment {

    private EditText groupMeetingID;
    private EditText displayNameEditor;
    private Button joinCallButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public GroupMeetingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getActivity().getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_group_meeting, container, false);
        joinCallButton = inflatedView.findViewById(R.id.group_call_join_next);
        joinCallButton.setOnClickListener(l -> joinCall());

        displayNameEditor = inflatedView.findViewById(R.id.group_call_display_name);
        final String savedDisplayName = sharedPreferences.getString(Constants.ACS_DISPLAY_NAME, "");
        if(savedDisplayName.length() > 0) {
            displayNameEditor.setText(savedDisplayName);
        }
        groupMeetingID = inflatedView.findViewById(R.id.group_call_id);
        return inflatedView;
    }



    private void joinCall() {
        final String displayName = displayNameEditor.getText().toString();
        final String groupCallId = groupMeetingID.getText().toString().trim();
        groupMeetingID.setText(groupCallId);

        if(displayName.isEmpty() || !isUUID(groupCallId)) {
            // Show error card
            return ;
        }

        editor.putString(Constants.ACS_DISPLAY_NAME, displayName);

        editor.putString(Constants.ACS_MEETING_ID, groupCallId);
        editor.apply();
        final CallComposite composite = new CallCompositeBuilder()
                .build();
        AzureUICalling calling = (AzureUICalling) requireActivity().getApplicationContext();
        calling.createCallingContext();
        CallingContext callingContext = calling.getCallingContext();
        CallCompositeRemoteOptions remoteOptions = new CallCompositeRemoteOptions(new CallCompositeGroupCallLocator(UUID.fromString(groupCallId)),callingContext.getCommunicationTokenCredential(),displayName);
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