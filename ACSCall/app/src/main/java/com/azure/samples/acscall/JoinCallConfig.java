// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall;

import java.io.Serializable;

public class JoinCallConfig implements Serializable {
    private final String groupId;
    private final boolean isMicrophoneMuted;
    private final boolean isCameraOn;
    private final String displayName;

    public JoinCallConfig(
            final String groupId,
            final boolean isMicrophoneMuted,
            final boolean isCameraOn,
            final String displayName) {
        this.groupId = groupId;
        this.isMicrophoneMuted = isMicrophoneMuted;
        this.isCameraOn = isCameraOn;
        this.displayName = displayName;
    }

    public String getGroupId() {
        return groupId;
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
