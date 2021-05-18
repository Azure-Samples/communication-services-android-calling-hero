// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.azure.android.communication.calling.CallingCommunicationException;
import com.azure.android.communication.calling.CreateViewOptions;
import com.azure.android.communication.calling.LocalVideoStream;
import com.azure.android.communication.calling.RemoteVideoStream;
import com.azure.android.communication.calling.VideoStreamRenderer;
import com.azure.android.communication.calling.VideoStreamRendererView;
import com.azure.android.communication.calling.ScalingMode;
import com.azure.samples.communication.calling.R;

public class ParticipantView extends RelativeLayout {
    // participant view properties
    private VideoStreamRenderer renderer;
    private VideoStreamRendererView rendererView;
    private String videoStreamId;

    // layout properties
    private final TextView title;
    private final ImageView defaultAvatar;
    private final ConstraintLayout videoContainer;
    private final FrameLayout activeSpeakerFrame;
    private final ImageButton switchCameraButton;
    private Runnable switchCameraOnClickAction;

    public ParticipantView(@NonNull final Context context) {
        super(context);
        inflate(context, R.layout.participant_view, this);
        this.title = findViewById(R.id.display_name);
        this.defaultAvatar = findViewById(R.id.default_avatar);
        this.videoContainer = findViewById(R.id.video_container);
        this.activeSpeakerFrame = findViewById(R.id.active_speaker_frame);
        this.switchCameraButton = findViewById(R.id.participant_switch_camera_button);
        switchCameraButton.setOnClickListener(l -> {
            if (switchCameraOnClickAction != null) {
                switchCameraButton.setEnabled(false);
                switchCameraOnClickAction.run();
            }
        });
    }

    public void setVideoStream(final RemoteVideoStream remoteVideoStream) {
        if (remoteVideoStream == null) {
            cleanUpVideoRendering();
            setVideoDisplayed(false);
            return;
        }

        final String newVideoStreamId = "RemoteVideoStream:" + remoteVideoStream.getId();
        if (newVideoStreamId.equals(videoStreamId)) {
            return;
        }

        try {
            final VideoStreamRenderer videoRenderer = new VideoStreamRenderer(remoteVideoStream, getContext());
            setVideoRenderer(videoRenderer);
            this.videoStreamId = newVideoStreamId;
        } catch (final CallingCommunicationException e) {
            e.printStackTrace();
        }
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

    public void setDisplayName(final String displayName) {
        this.title.setText(displayName);
    }

    public void setIsMuted(final Boolean isMuted) {
        final Drawable drawable = isMuted
            ? ContextCompat.getDrawable(getContext(), R.drawable.ic_fluent_mic_off_16_filled)
            : null;
        this.title.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public void setIsSpeaking(final Boolean isSpeaking) {
        this.activeSpeakerFrame.setVisibility(isSpeaking ? View.VISIBLE : View.INVISIBLE);
    }

    public void setDisplayNameVisible(final boolean isDisplayNameVisible) {
        this.title.setVisibility(isDisplayNameVisible ? VISIBLE : INVISIBLE);
    }

    public void setVideoDisplayed(final boolean isDisplayVideo) {
        defaultAvatar.setVisibility(isDisplayVideo ? INVISIBLE : VISIBLE);
    }

    public void setSwitchCameraButtonDisplayed(final boolean shouldShowButton) {
        switchCameraButton.setVisibility(shouldShowButton ? VISIBLE : GONE);
    }

    public void setImageButtonOnClickAction(final Runnable onClickAction) {
        switchCameraOnClickAction = onClickAction;
    }

    private void setVideoRenderer(final VideoStreamRenderer videoRenderer) {
        this.renderer = videoRenderer;
        this.rendererView = videoRenderer.createView(new CreateViewOptions(ScalingMode.CROP));
        attachRendererView(rendererView);
    }

    private void attachRendererView(final VideoStreamRendererView rendererView) {
        this.rendererView = rendererView;
        if (rendererView != null) {
            this.defaultAvatar.setVisibility(View.GONE);
            detachFromParentView(rendererView);
            this.videoContainer.addView(rendererView, 0);
            switchCameraButton.setEnabled(true);
        } else {
            this.defaultAvatar.setVisibility(View.VISIBLE);
        }
    }

    public void cleanUpVideoRendering() {
        disposeRenderView(this.rendererView);
        disposeRenderer(this.renderer);
        videoStreamId = null;
    }

    private void disposeRenderer(final VideoStreamRenderer renderer) {
        if (renderer != null) {
            renderer.dispose();
        }
    }

    private void disposeRenderView(final VideoStreamRendererView rendererView) {
        detachFromParentView(rendererView);
        if (rendererView != null) {
            rendererView.dispose();
        }
    }

    private void detachFromParentView(final VideoStreamRendererView rendererView) {
        if (rendererView != null && rendererView.getParent() != null) {
            ((ViewGroup) rendererView.getParent()).removeView(rendererView);
        }
    }
}
