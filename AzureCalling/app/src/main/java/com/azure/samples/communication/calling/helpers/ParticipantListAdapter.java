// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.azure.samples.communication.calling.R;

import java.util.List;
import java.util.stream.Collectors;

public class ParticipantListAdapter extends BottomCellAdapter {

    public ParticipantListAdapter(final Context context) {
        super(context);
    }

    public void setParticipantItems(final Context context, final List<ParticipantInfo> participantInfo) {
        final List<BottomCellItem> bottomCellItems =
                participantInfo.stream().map(participantInfo1 -> new BottomCellItem(ContextCompat.getDrawable(context,
                        R.drawable.ic_no_camera_50_64), participantInfo1.getDisplayName(),
                        ContextCompat.getDrawable(context, R.drawable.ic_fluent_mic_off_24_filled),
                        participantInfo1.getIsMuted(), null)).collect(Collectors.toList());

        setBottomCellItems(bottomCellItems);
        super.notifyDataSetChanged();
    }
}
