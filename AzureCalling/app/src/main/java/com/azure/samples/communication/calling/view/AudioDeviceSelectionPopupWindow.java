// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.view;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.external.calling.CallingContext;
import com.azure.samples.communication.calling.helpers.AudioDeviceType;
import com.azure.samples.communication.calling.helpers.AudioSessionManager;
import com.azure.samples.communication.calling.helpers.BottomCellAdapter;
import com.azure.samples.communication.calling.helpers.BottomCellItem;

import java.util.ArrayList;
import java.util.List;

public class AudioDeviceSelectionPopupWindow extends PopupWindow {
    private static final String LOG_TAG = AudioDeviceSelectionPopupWindow.class.getSimpleName();
    private Context context;
    private AudioSessionManager audioSessionManager;
    private CallingContext callingContext;

    public AudioDeviceSelectionPopupWindow(final Context context,
                                           final AudioSessionManager audioSessionManager,
                                           final CallingContext callingContext) {
        super(context);
        this.context = context;
        this.audioSessionManager = audioSessionManager;
        this.callingContext = callingContext;
    }

    @Override
    public void setContentView(final View contentView) {
        super.setContentView(contentView);

        final AudioDeviceType[] audioDeviceTypes = audioSessionManager.getAudioDeviceTypes();
        final AudioDeviceType currentAudioDeviceType = audioSessionManager.getCurrentAudioDeviceType();
        final List<BottomCellItem> bottomCellViewHolders = new ArrayList<>();
        for (AudioDeviceType audioDeviceType : audioDeviceTypes) {
            final BottomCellItem bottomCellItem = new BottomCellItem();
            if (audioDeviceType == AudioDeviceType.ANDROID) {
                bottomCellItem.setAvatar(ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_regular));
                bottomCellItem.setTitle("Android");
                bottomCellItem.setEnabled(currentAudioDeviceType == audioDeviceType);
                bottomCellItem.setOnClickAction(() -> setDeviceType(AudioDeviceType.ANDROID));
            } else if (audioDeviceType == AudioDeviceType.SPEAKER) {
                bottomCellItem.setAvatar(ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_filled));
                bottomCellItem.setTitle("Speaker");
                bottomCellItem.setEnabled(currentAudioDeviceType == audioDeviceType);
                bottomCellItem.setOnClickAction(() -> setDeviceType(AudioDeviceType.SPEAKER));
            }
            bottomCellItem.setAccessoryImage(ContextCompat.getDrawable(context,
                    R.drawable.ic_fluent_checkmark_24_regular));
            bottomCellViewHolders.add(bottomCellItem);
        }

        // Pass audio device data to RecyclerView Adapter
        final BottomCellAdapter bottomCellAdapter = new BottomCellAdapter(context, bottomCellViewHolders);
        final RecyclerView audioTable = (RecyclerView) contentView.findViewById(R.id.audio_device_table);
        audioTable.setAdapter(bottomCellAdapter);
        audioTable.setLayoutManager(new LinearLayoutManager(context));

        contentView.findViewById(R.id.overlay).setOnClickListener(v -> {
            dismiss();
            callingContext.setPopupWindowVisible(false);
        });
    }

    private void setDeviceType(final AudioDeviceType audioDeviceType) {
        audioSessionManager.switchAudioDeviceType(audioDeviceType);
        dismiss();
        callingContext.setPopupWindowVisible(false);
    }
}
