// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import androidx.annotation.StringRes;

import com.azure.samples.communication.calling.R;

public enum AudioDeviceType {
    ANDROID(R.string.audio_device_android),
    SPEAKER(R.string.audio_device_speaker);

    private @StringRes int audioDeviceText;
    AudioDeviceType(final int audioDeviceText) {
        this.audioDeviceText = audioDeviceText;
    }

    public int getAudioDeviceText() {
        return audioDeviceText;
    }
}
