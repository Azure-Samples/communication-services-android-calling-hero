// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.external.calling;

import java.io.Serializable;

public class JoinCallConfig implements Serializable {
    private final String joinId;
    private final boolean isMicrophoneMuted;
    private final boolean isCameraOn;
    private final String displayName;

    public JoinCallConfig(
            final String groupId,
            final boolean isMicrophoneMuted,
            final boolean isCameraOn,
            final String displayName) {
        this.joinId = groupId;
        this.isMicrophoneMuted = isMicrophoneMuted;
        this.isCameraOn = isCameraOn;
        this.displayName = displayName;
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
}
