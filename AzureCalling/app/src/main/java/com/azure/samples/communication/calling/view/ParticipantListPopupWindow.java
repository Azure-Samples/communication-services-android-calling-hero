// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.helpers.ParticipantInfo;
import com.azure.samples.communication.calling.helpers.ParticipantListAdapter;

import java.util.List;

public class ParticipantListPopupWindow extends PopupWindow {
    private Context context;
    private List<ParticipantInfo> participantInfo;

    public ParticipantListPopupWindow(final Context context, final List<ParticipantInfo> participantInfo) {
        super(context);
        this.context = context;
        this.participantInfo = participantInfo;
        final LayoutInflater layoutInflater
                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.activity_audio_selection, null);
        this.setContentView(layout);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(0x80000000));
        this.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void setContentView(final View contentView) {
        super.setContentView(contentView);

        // Pass audio device data to RecyclerView Adapter
        final ParticipantListAdapter bottomCellAdapter = new ParticipantListAdapter(this, context, participantInfo);
        final RecyclerView participantList = contentView.findViewById(R.id.audio_device_table);
        participantList.setAdapter(bottomCellAdapter);
        participantList.setLayoutManager(new LinearLayoutManager(context));

        contentView.findViewById(R.id.overlay).setOnClickListener(v -> {
            dismiss();
        });
    }
}
