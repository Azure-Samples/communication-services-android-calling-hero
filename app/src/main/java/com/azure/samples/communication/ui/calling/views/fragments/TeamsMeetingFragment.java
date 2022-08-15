package com.azure.samples.communication.ui.calling.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import com.azure.android.communication.ui.calling.CallComposite;
import com.azure.android.communication.ui.calling.CallCompositeBuilder;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.samples.communication.ui.calling.AzureUICalling;
import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.contracts.Constants;
import com.azure.samples.communication.ui.calling.contracts.SampleErrorMessages;
import com.azure.samples.communication.ui.calling.externals.calling.CallingContext;
import com.microsoft.fluentui.widget.Button;

/**
 * Fragment to host teams call meetings
 */
public class TeamsMeetingFragment extends AbstractBaseFragment {

    private EditText teamsDisplayNameEditor;
    private EditText teamsMeetingLink;
    private Button teamsJoinMeetingButton;

    public TeamsMeetingFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_teams_meeting, container, false);
        teamsJoinMeetingButton = inflatedView.findViewById(R.id.teams_call_join_next);
        teamsJoinMeetingButton.setOnClickListener(l -> joinTeamsCall());

        teamsDisplayNameEditor = inflatedView.findViewById(R.id.teams_call_display_name);

        final String savedDisplayName = getSharedPreferences().getString(Constants.ACS_DISPLAY_NAME, null);
        if(!TextUtils.isEmpty(savedDisplayName)) {
            teamsDisplayNameEditor.setText(savedDisplayName);
        }
        teamsMeetingLink = inflatedView.findViewById(R.id.teams_call_link);
        final String savedMeetingLink = getSharedPreferences().getString(Constants.ACS_MEETING_LINK, null);
        if (!TextUtils.isEmpty(savedMeetingLink)){
            teamsMeetingLink.setText(savedMeetingLink);
        }

        return inflatedView;
    }

    private void joinTeamsCall() {
        final String displayName = teamsDisplayNameEditor.getText().toString();
        final String teamsLink = teamsMeetingLink.getText().toString().trim();
        teamsMeetingLink.setText(teamsLink);

        if (TextUtils.isEmpty(teamsLink)){
            showError(SampleErrorMessages.TEAMS_LINK_REQUIRED);
            return;
        }

        if (!isValidWebURL(teamsLink)){
            showError(SampleErrorMessages.TEAMS_LINK_INVALID);
            return;
        }

        if (TextUtils.isEmpty(displayName)){
            showError(SampleErrorMessages.DISPLAY_NAME_REQUIRED);
            return ;
        }

        getSharedPreferences()
                .edit()
                .putString(Constants.ACS_DISPLAY_NAME, displayName)
                .putString(Constants.ACS_MEETING_LINK, teamsLink)
                .apply();

        final CallComposite composite = new CallCompositeBuilder()
                .build();
        AzureUICalling calling = (AzureUICalling) requireActivity().getApplicationContext();
        calling.createCallingContext();
        CallingContext callingContext = calling.getCallingContext();
        CallCompositeRemoteOptions remoteOptions = callingContext.getCallCompositeRemoteOptions(displayName, teamsLink);
        composite.addOnErrorEventHandler(callCompositeEventHandler);
        composite.launch(requireActivity(), remoteOptions);
    }

    private Boolean isValidWebURL(String url){
        return URLUtil.isValidUrl(url) && Patterns.WEB_URL.matcher(url).matches();
    }
}