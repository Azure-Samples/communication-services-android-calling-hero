// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.activities.CallActivity;
import com.azure.samples.communication.calling.view.BottomCellViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AudioSelectionAdapter extends BottomCellAdapter {

    private LayoutInflater mInflater;

    public AudioSelectionAdapter(final PopupWindow popupWindow,
                                 final Context context,
                                 final AudioSessionManager audioSessionManager) {
        super(context);
        final AudioDeviceType[] audioDeviceTypes = audioSessionManager.getAudioDeviceTypes();
        final AudioDeviceType currentAudioDeviceType = audioSessionManager.getCurrentAudioDeviceType();
        final List<BottomCellItem> bottomCellItems = new ArrayList<>();
        for (AudioDeviceType audioDeviceType : audioDeviceTypes) {
            final BottomCellItem bottomCellItem = new BottomCellItem();
            if (audioDeviceType == AudioDeviceType.ANDROID) {
                bottomCellItem.setAvatar(ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_regular));
                bottomCellItem.setTitle("Android");
                bottomCellItem.setEnabled(currentAudioDeviceType == audioDeviceType);
                bottomCellItem.setOnClickAction(() -> setDeviceType(AudioDeviceType.ANDROID,
                        context, audioSessionManager, popupWindow));
            } else if (audioDeviceType == AudioDeviceType.SPEAKER) {
                bottomCellItem.setAvatar(ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_filled));
                bottomCellItem.setTitle("Speaker");
                bottomCellItem.setEnabled(currentAudioDeviceType == audioDeviceType);
                bottomCellItem.setOnClickAction(() -> setDeviceType(AudioDeviceType.SPEAKER,
                        context, audioSessionManager, popupWindow));
            }
            bottomCellItem.setAccessoryImage(ContextCompat.getDrawable(context,
                    R.drawable.ic_fluent_checkmark_24_regular));
            bottomCellItems.add(bottomCellItem);
        }
        setBottomCellItems(bottomCellItems);
        this.mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public BottomCellViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = mInflater.inflate(R.layout.bottom_drawer_cell, parent, false);
        return new BottomCellViewHolder(view);
    }

    private void setDeviceType(final AudioDeviceType audioDeviceType,
                               final Context context,
                               final AudioSessionManager audioSessionManager,
                               final PopupWindow popupWindow) {
        audioSessionManager.switchAudioDeviceType(audioDeviceType);
        popupWindow.dismiss();
        if (context.getClass() == CallActivity.class) {
            ((CallActivity) context).setPopupWindowVisible(false);
        }
    }
}
