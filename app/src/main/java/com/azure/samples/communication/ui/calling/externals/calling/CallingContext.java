package com.azure.samples.communication.ui.calling.externals.calling;

import android.content.Context;
import android.telecom.Call;
import android.util.Log;

import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.common.CommunicationTokenRefreshOptions;
import com.azure.android.communication.ui.calling.models.CallCompositeGroupCallLocator;
import com.azure.android.communication.ui.calling.models.CallCompositeJoinLocator;
import com.azure.android.communication.ui.calling.models.CallCompositeRemoteOptions;
import com.azure.android.communication.ui.calling.models.CallCompositeTeamsMeetingLinkLocator;
import com.azure.samples.communication.ui.calling.contracts.CallType;

import java.util.UUID;
import java.util.concurrent.Callable;

public class CallingContext {

    Context appContext;
    private final Callable<String> tokenFetcher;
    private UUID joinUUID;
    private String joinId;

    public CallingContext(final Context applicationContext, final Callable<String> tokenFetcher) {
        this.tokenFetcher = tokenFetcher;
        appContext = applicationContext;
    }

    public CommunicationTokenCredential getCommunicationTokenCredential() {
        final CommunicationTokenRefreshOptions communicationTokenRefreshOptions =  new CommunicationTokenRefreshOptions (
                tokenFetcher, true );
        return new CommunicationTokenCredential(communicationTokenRefreshOptions);
    }

    public CallCompositeRemoteOptions getCallCompositeRemoteOptions(final String displayName) {
        final CallCompositeJoinLocator locator;
        joinUUID = UUID.randomUUID();
        joinId = joinUUID.toString();
        locator = new CallCompositeGroupCallLocator(joinUUID);

        return new CallCompositeRemoteOptions(locator,
                getCommunicationTokenCredential(),
                displayName);
    }

    public CallCompositeRemoteOptions getCallCompositeRemoteOptions(final String displayName,
                                                                    final String teamsLink) {
        final CallCompositeJoinLocator locator = new CallCompositeTeamsMeetingLinkLocator(teamsLink);
        return new CallCompositeRemoteOptions(locator,
                getCommunicationTokenCredential(),
                displayName);
    }

    public String getJoinId() {
        return joinId;
    }
}
