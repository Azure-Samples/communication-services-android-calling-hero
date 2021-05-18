// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.media.AudioManager;

public class AudioSessionManager {

    private AudioManager audioManager;

    public AudioSessionManager(final AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public AudioDeviceType[] getAudioDeviceTypes() {
        return new AudioDeviceType[]{AudioDeviceType.ANDROID, AudioDeviceType.SPEAKER};
    }

    public AudioDeviceType getCurrentAudioDeviceType() {
        if (audioManager.isSpeakerphoneOn()) {
            return AudioDeviceType.SPEAKER;
        }
        return AudioDeviceType.ANDROID;
    }

    public void switchAudioDeviceType(final AudioDeviceType audioDeviceType) {
        switch (audioDeviceType) {
            case ANDROID:
                setSpeakerPhoneStatus(false);
                break;
            case SPEAKER:
                setSpeakerPhoneStatus(true);
                break;
            default:
                throw new RuntimeException("The audio device type is illegal.");
        }
    }

    private void setSpeakerPhoneStatus(final boolean status) {
        audioManager.setSpeakerphoneOn(status);
    }
}
