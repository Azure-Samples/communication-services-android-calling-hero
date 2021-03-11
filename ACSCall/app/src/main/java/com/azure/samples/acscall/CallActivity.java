// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import androidx.lifecycle.Observer;

import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.azure.android.communication.calling.LocalVideoStream;
import com.azure.android.communication.calling.ParticipantState;
import com.azure.android.communication.calling.RemoteParticipant;
import com.azure.android.communication.calling.RemoteVideoStream;
import com.azure.samples.acscall.helpers.PermissionHelper;
import com.azure.samples.acscall.helpers.PermissionState;
import com.azure.samples.acscall.helpers.InCallService;
import com.azure.samples.acscall.view.ParticipantView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class CallActivity extends AppCompatActivity {

    private static final String LOG_TAG = CallActivity.class.getSimpleName();
    private static final int MIN_TIME_BETWEEN_PARTICIPANT_VIEW_UPDATES = 250;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CallingContext callingContext;
    private PermissionHelper permissionHelper;
    private Intent inCallServiceIntent;
    private GridLayout gridLayout;
    private ImageButton videoImageButton;
    private ImageButton audioImageButton;
    private LinearLayout callHangupOverlay;
    private View infoHeaderView;
    private Timer timer;
    private Integer localParticipantViewGridIndex;
    private Map<String, Integer> participantIdIndexPathMap;
    private List<ParticipantView> participantViewList;
    private ParticipantView localParticipantView;
    private ConstraintLayout localVideoViewContainer;
    private volatile boolean viewUpdatePending = false;
    private volatile long lastViewUpdateTimestamp = 0;
    private boolean callHangUpOverlaid;
    private Button callHangupConfirmButton;
    private Runnable initialVideoToggleRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScreenLayout();

        videoImageButton.setEnabled(false);
        audioImageButton.setEnabled(false);

        callingContext = ((ACSCall) getApplication()).getCallingContext();
        permissionHelper = ((ACSCall) getApplication()).getPermissionHelper();

        setVideoImageButtonEnabledState();

        participantViewList = new ArrayList<>();
        participantIdIndexPathMap = new HashMap<>();

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        /* initialize execution control of participant views update */
        initializeDisplayedParticipantsLiveData();

        /* get Join Call Config */
        JoinCallConfig joinCallConfig = (JoinCallConfig) getIntent().getSerializableExtra(Constants.JOIN_CALL_CONFIG);
        setLayoutComponentState(joinCallConfig.isMicrophoneMuted(), joinCallConfig.isCameraOn(),
                this.callHangUpOverlaid);

        // if the app is already in landscape mode, this check will hide status bar
        setStatusBarVisibility();

        callingContext.joinCallAsync(joinCallConfig).thenRun(() -> {
            runOnUiThread(() -> {
                /* initialize in-call notification icon */
                initializeCallNotification();

                audioImageButton.setEnabled(true);
                initParticipantViews();
                showParticipantHeaderNotification();
            });
        });
    }

    private void initializeDisplayedParticipantsLiveData() {
        final Observer<List<RemoteParticipant>> observerDisplayedRemoteParticipants = remoteParticipants -> {
            scheduleDelayedParticipantViewUpdate();
        };
        callingContext.getDisplayedParticipantsLiveData().observe(this, observerDisplayedRemoteParticipants);
    }

    private void scheduleDelayedParticipantViewUpdate() {
        handler.post(() -> {
            if (viewUpdatePending) {
                return;
            }
            viewUpdatePending = true;
            final long now = System.currentTimeMillis();
            final long timeElapsed = now - lastViewUpdateTimestamp;
            handler.postDelayed(() -> {
                updateParticipantViews();
                updateParticipantNotificationCount();
                lastViewUpdateTimestamp = System.currentTimeMillis();
                viewUpdatePending = false;
            }, Math.max(MIN_TIME_BETWEEN_PARTICIPANT_VIEW_UPDATES - timeElapsed, 0));
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setStatusBarVisibility();
        setupScreenLayout();
        setVideoImageButtonEnabledState();
        setLayoutComponentState(!callingContext.getMicOn(), callingContext.getCameraOn(), this.callHangUpOverlaid);
        gridLayout.post(() -> loadGridLayoutViews());
        if (localParticipantViewGridIndex == null) {
            setLocalParticipantView();
        }
    }

    private void setVideoImageButtonEnabledState() {
        final PermissionState videoAccess = permissionHelper.getVideoPermissionState(this);

        if (videoAccess == PermissionState.DENIED) {
            videoImageButton.setEnabled(false);
        } else if (videoAccess == PermissionState.NOT_ASKED) {
            initialVideoToggleRequest =
                    permissionHelper.createVideoPermissionRequest(this,
                            this::onInitialVideoToggleRequest);
            videoImageButton.setEnabled(true);
        } else {
            videoImageButton.setEnabled(true);
        }
    }

    private void setStatusBarVisibility() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            // Hide Status Bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            View decorView = getWindow().getDecorView();
            // Show Status Bar.
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "CallActivity onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "CallActivity onStop");
        callingContext.pauseVideo();
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "CallActivity onResume");
        callingContext.resumeVideo();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // disables back button in current screen.
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "CallActivity onDestroy");
        callingContext.getDisplayedParticipantsLiveData().removeObservers(this);
        if (localParticipantView != null) {
            localParticipantView.cleanUpVideoRendering();
            detachFromParentView(localParticipantView);
        }

        super.onDestroy();
    }

    private void initializeCallNotification() {
        inCallServiceIntent = new Intent(this, InCallService.class);
        startService(inCallServiceIntent);
    }

    private void initParticipantViews() {
        // load local participant's view
        localParticipantView = new ParticipantView(this);
        localParticipantView.updateDisplayName(callingContext.getDisplayName() + " (Me)");
        localParticipantView.updateVideoDisplayed(callingContext.getCameraOn());

        if (callingContext.getCameraOn()) {
            callingContext.getLocalVideoStreamCompletableFuture().thenAccept((localVideoStream -> {
                runOnUiThread(() -> localParticipantView.updateVideoStream(localVideoStream));
            }));
        } else {
            localParticipantView.updateVideoStream((LocalVideoStream) null);
        }

        // finalize the view data
        if (participantViewList.size() == 1) {
            setLocalParticipantView();
        } else {
            appendLocalParticipantView();
        }
        gridLayout.post(() -> loadGridLayoutViews());
    }

    private void updateParticipantViews() {
        List<ParticipantView> prevParticipantViewList = participantViewList;
        Map<String, Integer> preParticipantIdIndexPathMap = participantIdIndexPathMap;

        participantViewList = new ArrayList<>();
        participantIdIndexPathMap = new HashMap<>();

        List<RemoteParticipant> displayedRemoteParticipants =
                callingContext.getDisplayedParticipantsLiveData().getValue();
        int indexForNewParticipantViewList = 0;
        for (int i = 0; i < displayedRemoteParticipants.size(); i++) {
            RemoteParticipant remoteParticipant = displayedRemoteParticipants.get(i);
            ParticipantState participantState = remoteParticipant.getState();
            String id = callingContext.getId(remoteParticipant);
            if (ParticipantState.Disconnected == participantState) {
                continue;
            }
            ParticipantView pv;
            if (preParticipantIdIndexPathMap.containsKey(id)) {
                int prevIndex = preParticipantIdIndexPathMap.get(id);
                pv = prevParticipantViewList.get(prevIndex);
                RemoteVideoStream remoteVideoStream = getFirstVideoStream(remoteParticipant);
                pv.updateVideoStream(remoteVideoStream);
            } else {
                pv = new ParticipantView(this);
                pv.updateDisplayName(remoteParticipant.getDisplayName());
                RemoteVideoStream remoteVideoStream = getFirstVideoStream(remoteParticipant);
                pv.updateVideoStream(remoteVideoStream);
            }

            // update the participantIdIndexPathMap, participantViewList and participantsRenderList
            participantIdIndexPathMap.put(id, indexForNewParticipantViewList++);
            participantViewList.add(pv);
        }

        for (String id : preParticipantIdIndexPathMap.keySet()) {
            if (participantIdIndexPathMap.containsKey(id)) {
                continue;
            }
            ParticipantView discardedParticipantView =
                    prevParticipantViewList.get(preParticipantIdIndexPathMap.get(id));
            discardedParticipantView.cleanUpVideoRendering();
        }

        if (participantViewList.size() == 1) {
            if (localParticipantViewGridIndex != null) {
                localParticipantViewGridIndex = null;
            }
            setLocalParticipantView();
        } else {
            if (localParticipantViewGridIndex == null) {
                detachFromParentView(localParticipantView);
            }
            appendLocalParticipantView();
        }

        gridLayout.post(() -> {
            if ((prevParticipantViewList.size() > 1 && participantViewList.size() <= 1)
                    || (prevParticipantViewList.size() <= 1 && participantViewList.size() > 1)) {
                setupGridLayout();
            }
            updateGridLayoutViews();
        });
    }

    private void updateParticipantNotificationCount() {
        // if notification header is visible, the count will be updated, otherwise ignored
        setParticipantCountToFloatingHeader(callingContext.getRemoteParticipantCount());
    }

    private void showParticipantHeaderNotification() {
        updateParticipantNotificationCount();
        setFloatingHeaderVisibility(View.VISIBLE);
        infoHeaderView.bringToFront();
        initializeTimer();
    }

    private void toggleParticipantHeaderNotification() {
        if (infoHeaderView.getVisibility() == View.VISIBLE) {
            recycleTimer();
            setFloatingHeaderVisibility(View.GONE);
        } else {
            showParticipantHeaderNotification();
        }
    }

    private void initializeTimer() {
        recycleTimer();
        timer = new Timer();
        this.timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                setFloatingHeaderVisibility(View.GONE);
            }
        },  5000);
    }

    private void recycleTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void setParticipantCountToFloatingHeader(Integer text) {
        runOnUiThread(() ->
                ((TextView) findViewById(R.id.participantCountTextView)).setText(Integer.toString(text + 1)));
    }

    private void setFloatingHeaderVisibility(int visibility) {
        runOnUiThread(() -> infoHeaderView.setVisibility(visibility));
    }

    private void openShareDialogue() {
        Log.d(LOG_TAG, "Share button clicked!");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, callingContext.getGroupId());
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Group Call ID");
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void setLayoutComponentState(boolean isMicrophoneMuted, boolean isCameraOn, boolean isCallHangUpOverLaid) {
        audioImageButton.setSelected(!isMicrophoneMuted);
        videoImageButton.setSelected(isCameraOn);
        callHangupOverlay.setVisibility(isCallHangUpOverLaid ? View.VISIBLE : View.INVISIBLE);
    }

    private void openHangupDialog() {
        if (!callHangUpOverlaid) {
            callHangUpOverlaid = true;
            callHangupOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void closeHangupDialog() {
        if (callHangUpOverlaid) {
            callHangUpOverlaid = false;
            callHangupOverlay.setVisibility(View.GONE);
        }
    }

    private void hangup() {
        Log.d(LOG_TAG, "Hangup button clicked!");
        if (localParticipantView != null) {
            localParticipantView.cleanUpVideoRendering();
            detachFromParentView(localParticipantView);
        }
        stopService(inCallServiceIntent);
        callHangupConfirmButton.setEnabled(false);
        callingContext.hangupAsync();
        Intent intent = new Intent(this, EndActivity.class);
        startActivity(intent);
        finish();
    }

    private void toggleVideo(boolean isSelected) {
        if (!isSelected) {
            if (initialVideoToggleRequest != null) {
                initialVideoToggleRequest.run();
                // Video will turn on, or the button will be disabled
            } else {
                toggleVideoOn();
            }
        } else {
            toggleVideoOff();
        }
    }

    private void onInitialVideoToggleRequest() {
        PermissionState videoAccess = permissionHelper.getVideoPermissionState(this);
        if (videoAccess == PermissionState.GRANTED) {
            toggleVideoOn();
        } else {
            runOnUiThread(() -> {
                videoImageButton.setSelected(false);
                videoImageButton.setEnabled(false);
            });
        }
    }

    private void toggleVideoOn() {
        Log.d(LOG_TAG, "toggleVideo -> on");
        callingContext.turnOnVideoAsync().whenComplete((localVideoStream, throwable) -> {
            runOnUiThread(() -> {
                localParticipantView.updateVideoStream(localVideoStream);
                localParticipantView.updateVideoDisplayed(callingContext.getCameraOn());
                localVideoViewContainer.setVisibility(
                        (localParticipantViewGridIndex == null && !callingContext.getCameraOn())
                                ? View.INVISIBLE : View.VISIBLE);
                videoImageButton.setSelected(true);
            });
        });
    }

    private void toggleVideoOff() {
        Log.d(LOG_TAG, "toggleVideo -> off");
        callingContext.turnOffVideoAsync().whenComplete((aVoid, throwable) -> {
            runOnUiThread(() -> {
                localParticipantView.updateVideoStream((LocalVideoStream) null);
                localParticipantView.updateVideoDisplayed(callingContext.getCameraOn());
                localVideoViewContainer.setVisibility(
                        (localParticipantViewGridIndex == null && !callingContext.getCameraOn())
                                ? View.INVISIBLE : View.VISIBLE);
                videoImageButton.setSelected(false);
            });
        });
    }

    private void toggleAudio(boolean toggleOff) {
        if (!toggleOff) {
            callingContext.turnOnAudioAsync().whenComplete((aVoid, throwable) -> {
                audioImageButton.setSelected(true);
            });
        } else {
            callingContext.turnOffAudioAsync().whenComplete((aVoid, throwable) -> {
                audioImageButton.setSelected(false);
            });
        }
    }

    private void detachFromParentView(View view) {
        if (view != null && view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private RemoteVideoStream getFirstVideoStream(RemoteParticipant remoteParticipant) {
        if (remoteParticipant.getVideoStreams().size() > 0) {
            return remoteParticipant.getVideoStreams().get(0);
        }
        return null;
    }

    private void setupScreenLayout() {
        setContentView(R.layout.activity_call);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        videoImageButton = findViewById(R.id.call_video);
        videoImageButton.setOnClickListener(l -> toggleVideo(videoImageButton.isSelected()));

        audioImageButton = findViewById(R.id.call_audio);
        audioImageButton.setOnClickListener(l -> toggleAudio(audioImageButton.isSelected()));

        ImageButton shareButton = findViewById(R.id.call_share);
        shareButton.setOnClickListener(l -> openShareDialogue());

        ImageButton hangupButton = findViewById(R.id.call_hangup);
        hangupButton.setOnClickListener(l -> openHangupDialog());

        callHangupConfirmButton = findViewById(R.id.call_hangup_confirm);
        callHangupConfirmButton.setOnClickListener(l -> hangup());

        Button callHangupCancelButton = findViewById(R.id.call_hangup_cancel);
        callHangupCancelButton.setOnClickListener(l -> closeHangupDialog());

        infoHeaderView = findViewById(R.id.info_header);
        gridLayout = findViewById(R.id.groupCallTable);
        localVideoViewContainer = findViewById(R.id.yourCameraHolder);

        gridLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // close hangup dialog if open
                closeHangupDialog();

                toggleParticipantHeaderNotification();
                return true;
            }
            return false;
        });

        callHangupOverlay = findViewById(R.id.call_hangup_overlay);
    }

    private void setupGridLayout() {
        gridLayout.removeAllViews();
        if (participantViewList.size() <= 1) {
            gridLayout.setRowCount(1);
            gridLayout.setColumnCount(1);
            gridLayout.addView(createCellForGridLayout(gridLayout.getMeasuredWidth(),
                    gridLayout.getMeasuredHeight()));
        } else {
            gridLayout.setRowCount(2);
            gridLayout.setColumnCount(2);
            for (int i = 0; i < 4; i++) {
                gridLayout.addView(createCellForGridLayout(gridLayout.getMeasuredWidth() / 2,
                        gridLayout.getMeasuredHeight() / 2));
            }
        }
    }

    private void loadGridLayoutViews() {
        setupGridLayout();
        for (int i = 0; i < participantViewList.size(); i++) {
            ParticipantView participantView = participantViewList.get(i);
            detachFromParentView(participantView);
            ((LinearLayout) gridLayout.getChildAt(i)).addView(participantView, 0);
        }
    }

    private void setLocalParticipantView() {
        detachFromParentView(localParticipantView);
        localVideoViewContainer.setVisibility(callingContext.getCameraOn() ? View.VISIBLE : View.INVISIBLE);
        localParticipantView.updateDisplayNameVisible(false);
        localVideoViewContainer.addView(localParticipantView);
        localVideoViewContainer.bringToFront();
    }

    private void appendLocalParticipantView() {
        localParticipantViewGridIndex = participantViewList.size();
        localParticipantView.updateDisplayNameVisible(true);
        localParticipantView.updateVideoDisplayed(callingContext.getCameraOn());
        participantViewList.add(localParticipantView);
        localVideoViewContainer.setVisibility(View.INVISIBLE);
    }

    private void updateGridLayoutViews() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            LinearLayout wrap = (LinearLayout) gridLayout.getChildAt(i);
            ParticipantView preParticipantView = (ParticipantView) wrap.getChildAt(0);

            if (i >= participantViewList.size()) {
                wrap.removeAllViews();
            } else {
                ParticipantView newParticipantView = participantViewList.get(i);
                if (preParticipantView != newParticipantView) {
                    detachFromParentView(newParticipantView);
                    wrap.removeAllViews();
                    wrap.addView(newParticipantView, 0);
                }
            }
        }
    }

    private LinearLayout createCellForGridLayout(int width, int height) {
        LinearLayout cell = new LinearLayout(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = width;
        params.height = height;
        cell.setLayoutParams(params);
        return cell;
    }
}
