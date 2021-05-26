// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

public class ParticipantInfo {
    private String displayName;
    private Boolean isMuted;

    public ParticipantInfo(final String displayName, final Boolean isMuted) {
        this.displayName = displayName;
        this.isMuted = isMuted;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }
}
