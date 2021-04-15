// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.external.calling;

import com.azure.android.communication.calling.RemoteParticipant;

public class RemoteParticipantUpdate {

    private final RemoteParticipant remoteParticipant;
    private final RemoteParticipantUpdateType updateType;

    public RemoteParticipantUpdate(
            final RemoteParticipant remoteParticipant,
            final RemoteParticipantUpdateType updateType) {
        this.remoteParticipant = remoteParticipant;
        this.updateType = updateType;
    }

    public RemoteParticipant getRemoteParticipant() {
        return remoteParticipant;
    }

    public RemoteParticipantUpdateType getUpdateType() {
        return updateType;
    }
}
