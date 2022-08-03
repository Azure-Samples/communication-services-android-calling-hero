package com.azure.samples.communication.ui.calling.externals.calling;

import android.content.Context;
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

    public CallingContext(final Context applicationContext, final Callable<String> tokenFetcher) {
        this.tokenFetcher = tokenFetcher;
        appContext = applicationContext;
    }

    public CommunicationTokenCredential getCommunicationTokenCredential() {
        final CommunicationTokenRefreshOptions communicationTokenRefreshOptions =  new CommunicationTokenRefreshOptions (
                tokenFetcher, true );
        return new CommunicationTokenCredential(communicationTokenRefreshOptions);
    }

    public CallCompositeRemoteOptions getCallCompositeRemoteOptions(CallType callType) {
        final CallCompositeJoinLocator locator;
        if (CallType.GROUP_CALL.equals(callType)) {
            locator = new CallCompositeGroupCallLocator(UUID.randomUUID());
        } else {
            throw new IllegalStateException("Illegal value for CallType.");
        }

        return new CallCompositeRemoteOptions(locator,
                getCommunicationTokenCredential(),
                "Mohtasim");
    }
}
