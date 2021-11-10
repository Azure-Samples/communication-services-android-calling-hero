// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
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
    private ParticipantListAdapter bottomCellAdapter;

    public ParticipantListPopupWindow(final Context context, final List<ParticipantInfo> participantInfo) {
        super(context);
        this.context = context;
        final LayoutInflater layoutInflater
                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.buttom_drawer_view, null);
        this.setContentView(layout);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(0x80000000));
        setParticipantInfo(participantInfo);
    }

    @Override
    public void setContentView(final View contentView) {
        super.setContentView(contentView);

        bottomCellAdapter = new ParticipantListAdapter(context);
        final RecyclerView participantList = contentView.findViewById(R.id.bottom_drawer_table);
        participantList.setAdapter(bottomCellAdapter);
        participantList.setLayoutManager(new LinearLayoutManager(context));

        contentView.findViewById(R.id.overlay).setOnClickListener(v -> {
            dismiss();
        });
    }

    public void setParticipantInfo(final List<ParticipantInfo> participantInfo) {
        this.bottomCellAdapter.setParticipantItems(context, participantInfo);
    }
}
