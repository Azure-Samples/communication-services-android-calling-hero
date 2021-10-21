// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.azure.samples.communication.calling.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioSelectionAdapter extends BottomCellAdapter {

    private Map<AudioDeviceType, BottomCellItem> bottomCellItemState;
    public AudioSelectionAdapter(final Context context,
                                 final AudioSessionManager audioSessionManager,
                                 final Runnable onSelect) {
        super(context);
        final AudioDeviceType[] audioDeviceTypes = audioSessionManager.getAudioDeviceTypes();
        final AudioDeviceType currentAudioDeviceType = audioSessionManager.getCurrentAudioDeviceType();
        final List<BottomCellItem> bottomCellItems = new ArrayList<>();
        bottomCellItemState = new HashMap<>();
        for (AudioDeviceType audioDeviceType : audioDeviceTypes) {
            if (audioDeviceType == AudioDeviceType.ANDROID) {
                final BottomCellItem bottomCellItem = new BottomCellItem(
                        ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_regular),
                        "Android",
                    ContextCompat.getDrawable(context,
                                R.drawable.ic_fluent_checkmark_24_regular),
                        currentAudioDeviceType == audioDeviceType,
                    () -> setDeviceType(AudioDeviceType.ANDROID,
                        audioSessionManager, onSelect));
                bottomCellItems.add(bottomCellItem);
                bottomCellItemState.put(audioDeviceType, bottomCellItem);
            } else if (audioDeviceType == AudioDeviceType.SPEAKER) {
                final BottomCellItem bottomCellItem = new BottomCellItem(
                        ContextCompat.getDrawable(context,
                        R.drawable.ic_fluent_speaker_2_24_filled),
                        "Speaker",
                    ContextCompat.getDrawable(context,
                                R.drawable.ic_fluent_checkmark_24_regular),
                        currentAudioDeviceType == audioDeviceType,
                    () -> setDeviceType(AudioDeviceType.SPEAKER,
                                audioSessionManager, onSelect));
                bottomCellItems.add(bottomCellItem);
                bottomCellItemState.put(audioDeviceType, bottomCellItem);
            }
        }
        setBottomCellItems(bottomCellItems);
    }

    private void setDeviceType(final AudioDeviceType audioDeviceType,
                               final AudioSessionManager audioSessionManager,
                               final Runnable onSelect) {
        audioSessionManager.switchAudioDeviceType(audioDeviceType);
        for (final AudioDeviceType type : bottomCellItemState.keySet()) {
            final BottomCellItem bottomCellItem = bottomCellItemState.get(type);
            if (type == audioDeviceType) {
                bottomCellItem.setEnabled(true);
            } else {
                bottomCellItem.setEnabled(false);
            }
        }
        super.notifyDataSetChanged();
        onSelect.run();
    }
}
