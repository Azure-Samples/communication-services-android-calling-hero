// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.view;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.azure.android.communication.calling.CallingCommunicationException;
import com.azure.android.communication.calling.LocalVideoStream;
import com.azure.android.communication.calling.VideoStreamRenderer;
import com.azure.samples.communication.calling.R;


public class LocalParticipantView extends ParticipantView {
    // layout properties
    private final ConstraintLayout switchCameraButton;
    private Runnable switchCameraOnClickAction;

    public LocalParticipantView(@NonNull final Context context) {
        super(context);

        this.switchCameraButton = findViewById(R.id.participant_switch_camera_button);
        switchCameraButton.setOnClickListener(l -> {
            if (switchCameraOnClickAction != null) {
                switchCameraButton.setEnabled(false);
                switchCameraOnClickAction.run();
            }
        });
    }

    public void setVideoStream(final LocalVideoStream localVideoStream) {
        if (localVideoStream == null) {
            cleanUpVideoRendering();
            return;
        }

        final String newVideoStreamId = "LocalVideoStream:" + localVideoStream.getSource().getId();
        if (newVideoStreamId.equals(videoStreamId)) {
            return;
        }

        try {
            final VideoStreamRenderer videoRenderer = new VideoStreamRenderer(localVideoStream, getContext());
            setVideoRenderer(videoRenderer);
            this.videoStreamId = newVideoStreamId;
        } catch (final CallingCommunicationException e) {
            e.printStackTrace();
        }
    }

    public void setSwitchCameraButtonDisplayed(final boolean shouldShowButton) {
        switchCameraButton.setVisibility(shouldShowButton ? VISIBLE : GONE);
    }

    public void setSwitchCameraButtonEnabled(final boolean shouldEnable) {
        switchCameraButton.setEnabled(shouldEnable);
    }

    public void centerSwitchCameraButton(final boolean shouldCenter) {
        final ConstraintSet set = new ConstraintSet();
        final ConstraintLayout layout;

        layout = (ConstraintLayout) findViewById(R.id.video_container);
        set.clone(layout);
        if (shouldCenter) {
            set.connect(switchCameraButton.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM,
                    4);
        } else {
            set.clear(switchCameraButton.getId(), ConstraintSet.BOTTOM);
        }
        set.applyTo(layout);
    }

    public void setSwitchCameraButtonOnClickAction(final Runnable onClickAction) {
        switchCameraOnClickAction = onClickAction;
    }
}
