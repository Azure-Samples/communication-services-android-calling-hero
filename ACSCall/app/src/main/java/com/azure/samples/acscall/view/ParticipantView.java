// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.azure.android.communication.calling.CommunicationException;
import com.azure.android.communication.calling.LocalVideoStream;
import com.azure.android.communication.calling.RemoteVideoStream;
import com.azure.android.communication.calling.Renderer;
import com.azure.android.communication.calling.RendererView;
import com.azure.android.communication.calling.RenderingOptions;
import com.azure.android.communication.calling.ScalingMode;
import com.azure.samples.acscall.R;

public class ParticipantView extends RelativeLayout {

    private static final String LOG_TAG = ParticipantView.class.getSimpleName();

    // participant view properties
    private Renderer renderer;
    private RendererView rendererView;
    private int remoteVideoStreamId;
    private String localVideoSourceId;
    private Context context;

    // layout properties
    private TextView title;
    private ImageView defaultAvatar;
    private ConstraintLayout videoContainer;


    public ParticipantView(@NonNull Context context) {
        super(context);
        this.context = context;
        inflate(context, R.layout.participant_view, this);
        this.title = findViewById(R.id.display_name);
        this.defaultAvatar = findViewById(R.id.default_avatar);
        this.videoContainer = findViewById(R.id.video_container);
    }

    public void updateVideoStream(RemoteVideoStream remoteVideoStream) {

        if (remoteVideoStream == null) {
            cleanUpVideoRendering();
            updateVideoDisplayed(false);
            return;
        }

        if (remoteVideoStream.getId() == remoteVideoStreamId) {
            return;
        }

        if (remoteVideoStream != null) {
            try {
                Renderer videoRenderer = new Renderer(remoteVideoStream, context);
                updateRendering(videoRenderer);
                this.remoteVideoStreamId = remoteVideoStream.getId();
            } catch (CommunicationException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateVideoStream(LocalVideoStream localVideoStream) {

        if (localVideoStream == null) {
            cleanUpVideoRendering();
            return;
        }

        if (localVideoStream.getSource().getId() == localVideoSourceId) {
            return;
        }

        if (localVideoStream != null) {
            try {
                Renderer videoRenderer = new Renderer(localVideoStream, context);
                updateRendering(videoRenderer);
                this.localVideoSourceId = localVideoStream.getSource().getId();
            } catch (CommunicationException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateDisplayName(String displayName) {
        this.title.setText(displayName);
    }

    public void updateDisplayNameVisible(boolean isDisplayNameVisible) {
        this.title.setVisibility(isDisplayNameVisible ? VISIBLE : INVISIBLE);
    }

    public void updateVideoDisplayed(boolean isDisplayVideo) {
        defaultAvatar.setVisibility(isDisplayVideo ? INVISIBLE : VISIBLE);
    }

    private void updateRendering(Renderer videoRenderer) {
        this.renderer = videoRenderer;
        this.rendererView = videoRenderer.createView(new RenderingOptions(ScalingMode.Crop));
        attachRendererView(rendererView);
    }

    private void attachRendererView(RendererView rendererView) {
        this.rendererView = rendererView;
        if (rendererView != null) {
            this.defaultAvatar.setVisibility(View.GONE);
            detachFromParentView(rendererView);
            this.videoContainer.addView(rendererView, 0);
        } else {
            this.defaultAvatar.setVisibility(View.VISIBLE);
        }
    }

    public void cleanUpVideoRendering() {
        disposeRenderView(this.rendererView);
        disposeRenderer(this.renderer);
    }

    private void disposeRenderer(Renderer renderer) {
        if (renderer != null) {
            renderer.dispose();
        }
    }

    private void disposeRenderView(RendererView rendererView) {
        detachFromParentView(rendererView);
        if (rendererView != null) {
            rendererView.dispose();
        }
    }

    private void detachFromParentView(RendererView rendererView) {
        if (rendererView != null && rendererView.getParent() != null) {
            ((ViewGroup) rendererView.getParent()).removeView(rendererView);
        }
    }
}
