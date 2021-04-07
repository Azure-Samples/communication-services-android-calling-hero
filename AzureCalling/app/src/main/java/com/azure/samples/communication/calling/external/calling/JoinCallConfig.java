// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.external.calling;

import com.azure.samples.communication.calling.helpers.JoinCallType;

import java.io.Serializable;

public class JoinCallConfig implements Serializable {
    private final String joinId;
    private final boolean isMicrophoneMuted;
    private final boolean isCameraOn;
    private final String displayName;
    private final JoinCallType callType;

    public JoinCallConfig(
            final String joinId,
            final boolean isMicrophoneMuted,
            final boolean isCameraOn,
            final String displayName,
            final JoinCallType callType) {
        this.joinId = joinId;
        this.isMicrophoneMuted = isMicrophoneMuted;
        this.isCameraOn = isCameraOn;
        this.displayName = displayName;
        this.callType = callType;
    }

    public String getJoinId() {
        return joinId;
    }

    public boolean isMicrophoneMuted() {
        return isMicrophoneMuted;
    }

    public boolean isCameraOn() {
        return isCameraOn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public JoinCallType getCallType() {
        return callType;
    }
}
