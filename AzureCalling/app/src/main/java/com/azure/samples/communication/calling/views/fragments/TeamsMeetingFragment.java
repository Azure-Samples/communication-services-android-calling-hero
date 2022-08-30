// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.fragments;

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
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.contracts.Constants;
import com.azure.samples.communication.calling.contracts.SampleErrorMessages;
import com.azure.samples.communication.calling.externals.calling.CallingContext;
import com.azure.samples.communication.calling.utilities.AppSettings;
import com.microsoft.fluentui.widget.Button;

/**
 * Fragment to host teams call meetings
 */
public class TeamsMeetingFragment extends AbstractBaseFragment {

    private EditText teamsDisplayNameEditor;
    private EditText teamsMeetingLink;
    private Button teamsJoinMeetingButton;
    private AppSettings appSettings;

    public TeamsMeetingFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        appSettings = ((AzureCalling) requireActivity().getApplicationContext()).getAppSettings();
        final View inflatedView = inflater.inflate(R.layout.fragment_teams_meeting, container,
                false);
        teamsJoinMeetingButton = inflatedView.findViewById(R.id.teams_call_join_next);
        teamsJoinMeetingButton.setOnClickListener(l -> joinTeamsCall());

        teamsDisplayNameEditor = inflatedView.findViewById(R.id.teams_call_display_name);

        final String savedDisplayName = appSettings.getUserProfile().getDisplayName();
        if (!TextUtils.isEmpty(savedDisplayName)) {
            teamsDisplayNameEditor.setText(savedDisplayName);
        }
        teamsMeetingLink = inflatedView.findViewById(R.id.teams_call_link);
        final String savedMeetingLink = getSharedPreferences().getString(Constants.ACS_MEETING_LINK, "");
        if (!TextUtils.isEmpty(savedMeetingLink)) {
            teamsMeetingLink.setText(savedMeetingLink);
        }

        return inflatedView;
    }

    private void joinTeamsCall() {
        final String displayName = teamsDisplayNameEditor.getText().toString();
        final String teamsLink = teamsMeetingLink.getText().toString().trim();
        teamsMeetingLink.setText(teamsLink);
        appSettings.getUserProfile().setDisplayName(displayName);
        if (TextUtils.isEmpty(teamsLink)) {
            showError(SampleErrorMessages.TEAMS_LINK_REQUIRED);
            return;
        }

        if (!isValidWebURL(teamsLink)) {
            showError(SampleErrorMessages.TEAMS_LINK_INVALID);
            return;
        }

        if (TextUtils.isEmpty(displayName)) {
            showError(SampleErrorMessages.DISPLAY_NAME_REQUIRED);
            return;
        }

        getSharedPreferences()
                .edit()
                .putString(Constants.ACS_MEETING_LINK, teamsLink)
                .apply();

        if (appSettings.getUserProfile().getUsername().isEmpty()) {
            appSettings.getUserProfile().setUsername(displayName);
        }
        final CallComposite composite = new CallCompositeBuilder()
                .build();
        final AzureCalling calling = (AzureCalling) requireActivity().getApplicationContext();
        calling.createCallingContext();
        final CallingContext callingContext = calling.getCallingContext();
        final CallCompositeRemoteOptions remoteOptions = callingContext
                                            .getCallCompositeRemoteOptions(displayName, teamsLink);
        composite.addOnErrorEventHandler(callCompositeEventHandler);
        composite.launch(requireActivity(), remoteOptions);
    }

    private Boolean isValidWebURL(final String url) {
        return URLUtil.isValidUrl(url) && Patterns.WEB_URL.matcher(url).matches();
    }
}
