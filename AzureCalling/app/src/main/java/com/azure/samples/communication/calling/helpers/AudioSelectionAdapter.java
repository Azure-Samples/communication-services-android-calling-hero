// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.content.Context;
import android.widget.PopupWindow;
import androidx.core.content.ContextCompat;
import com.azure.samples.communication.calling.R;

import java.util.ArrayList;
import java.util.List;

public class AudioSelectionAdapter extends BottomCellAdapter {

    public AudioSelectionAdapter(final PopupWindow popupWindow,
                                 final Context context,
                                 final AudioSessionManager audioSessionManager) {
        super(context);
        final AudioDeviceType[] audioDeviceTypes = audioSessionManager.getAudioDeviceTypes();
        final AudioDeviceType currentAudioDeviceType = audioSessionManager.getCurrentAudioDeviceType();
        final List<BottomCellItem> bottomCellItems = new ArrayList<>();
        for (AudioDeviceType audioDeviceType : audioDeviceTypes) {
            if (audioDeviceType == AudioDeviceType.ANDROID) {
                final BottomCellItem bottomCellItem = new BottomCellItem(ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_regular),
                        "Android",
                    ContextCompat.getDrawable(context,
                                R.drawable.ic_fluent_checkmark_24_regular),
                        currentAudioDeviceType == audioDeviceType,
                    () -> setDeviceType(AudioDeviceType.ANDROID,
                        audioSessionManager, popupWindow));
                bottomCellItems.add(bottomCellItem);
            } else if (audioDeviceType == AudioDeviceType.SPEAKER) {
                final BottomCellItem bottomCellItem = new BottomCellItem(ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_filled),
                        "Speaker",
                    ContextCompat.getDrawable(context,
                                R.drawable.ic_fluent_checkmark_24_regular),
                        currentAudioDeviceType == audioDeviceType,
                    () -> setDeviceType(AudioDeviceType.SPEAKER,
                                audioSessionManager, popupWindow));
                bottomCellItems.add(bottomCellItem);
            }
        }
        setBottomCellItems(bottomCellItems);
    }

    private void setDeviceType(final AudioDeviceType audioDeviceType,
                               final AudioSessionManager audioSessionManager,
                               final PopupWindow popupWindow) {
        audioSessionManager.switchAudioDeviceType(audioDeviceType);
        popupWindow.dismiss();
    }
}
