package com.azure.samples.communication.ui.calling.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import java.util.UUID;

/**
 * Fragment to host teams call meetings
 */
public class TeamsMeetingFragment extends Fragment {

    private EditText teamsDisplayNameEditor;
    private EditText teamsMeetingLink;
    private Button teamsJoinMeetingButton;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TeamsMeetingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getActivity().getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_teams_meeting, container, false);
        teamsJoinMeetingButton = inflatedView.findViewById(R.id.teams_call_join_next);
        teamsJoinMeetingButton.setOnClickListener(l -> joinTeamsCall());

        teamsDisplayNameEditor = inflatedView.findViewById(R.id.teams_call_display_name);
        final String savedDisplayName = sharedPreferences.getString(Constants.ACS_DISPLAY_NAME, "");
        if(savedDisplayName.length() > 0) {
            teamsDisplayNameEditor.setText(savedDisplayName);
        }
        teamsMeetingLink = inflatedView.findViewById(R.id.teams_call_link);

        return inflatedView;
    }

    private void joinTeamsCall() {
        final String displayName = teamsDisplayNameEditor.getText().toString();
        final String teamsLink = teamsMeetingLink.getText().toString().trim();
        teamsMeetingLink.setText(teamsLink);

        if(displayName.isEmpty()) {
            // Show error card
            return ;
        }

        editor.putString(Constants.ACS_DISPLAY_NAME, displayName);
        editor.apply();

        final CallComposite composite = new CallCompositeBuilder()
                .build();
        AzureUICalling calling = (AzureUICalling) requireActivity().getApplicationContext();
        calling.createCallingContext();
        CallingContext callingContext = calling.getCallingContext();
        CallCompositeRemoteOptions remoteOptions = callingContext.getCallCompositeRemoteOptions(displayName, teamsLink);
        composite.launch(requireActivity(), remoteOptions);
    }


}