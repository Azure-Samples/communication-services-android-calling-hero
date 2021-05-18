// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

public class ParticipantInfo {
    private String displayName;
    private Boolean ismuted;

    public ParticipantInfo(final String displayName, final Boolean ismuted) {
        this.displayName = displayName;
        this.ismuted = ismuted;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Boolean getIsmuted() {
        return ismuted;
    }
}
