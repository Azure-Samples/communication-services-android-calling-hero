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
import com.azure.samples.communication.calling.activities.CallActivity;
import com.azure.samples.communication.calling.helpers.AudioSelectionAdapter;
import com.azure.samples.communication.calling.helpers.AudioSessionManager;

public class AudioDeviceSelectionPopupWindow extends PopupWindow {
    private static final String LOG_TAG = AudioDeviceSelectionPopupWindow.class.getSimpleName();
    private Context context;
    private AudioSessionManager audioSessionManager;

    public AudioDeviceSelectionPopupWindow(final Context context,
                                           final AudioSessionManager audioSessionManager) {
        super(context);
        this.context = context;
        this.audioSessionManager = audioSessionManager;
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
        final AudioSelectionAdapter bottomCellAdapter = new AudioSelectionAdapter(this, context, audioSessionManager);
        final RecyclerView audioTable = contentView.findViewById(R.id.audio_device_table);
        audioTable.setAdapter(bottomCellAdapter);
        audioTable.setLayoutManager(new LinearLayoutManager(context));

        contentView.findViewById(R.id.overlay).setOnClickListener(v -> {
            dismiss();
            if (context.getClass() == CallActivity.class) {
                ((CallActivity) context).setPopupWindowVisible(false);
            }
        });
    }
}
