// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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
import android.view.Gravity;
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
import com.azure.android.communication.calling.MediaStreamType;
import com.azure.android.communication.calling.ParticipantState;
import com.azure.android.communication.calling.RemoteParticipant;
import com.azure.android.communication.calling.RemoteVideoStream;
import com.azure.samples.communication.calling.AzureCalling;
import com.azure.samples.communication.calling.external.calling.CallingContext;
import com.azure.samples.communication.calling.helpers.AudioSessionManager;
import com.azure.samples.communication.calling.helpers.Constants;
import com.azure.samples.communication.calling.external.calling.JoinCallConfig;
import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.helpers.ParticipantInfo;
import com.azure.samples.communication.calling.helpers.PermissionHelper;
import com.azure.samples.communication.calling.helpers.PermissionState;
import com.azure.samples.communication.calling.helpers.InCallService;
import com.azure.samples.communication.calling.view.AudioDeviceSelectionPopupWindow;
import com.azure.samples.communication.calling.view.LocalParticipantView;
import com.azure.samples.communication.calling.view.ParticipantListPopupWindow;
import com.azure.samples.communication.calling.view.ParticipantView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class CallActivity extends AppCompatActivity {

    private static final String LOG_TAG = CallActivity.class.getSimpleName();

    private static final int MIN_TIME_BETWEEN_PARTICIPANT_VIEW_UPDATES = 500;
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
    private LocalParticipantView localParticipantView;
    private ConstraintLayout localVideoViewContainer;
    private volatile boolean viewUpdatePending = false;
    private boolean callHangUpOverlaid;
    private Button callHangupConfirmButton;
    private Runnable initialVideoToggleRequest;
    private AudioDeviceSelectionPopupWindow audioDeviceSelectionPopupWindow;
    private ParticipantListPopupWindow participantListPopupWindow;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScreenLayout();

        videoImageButton.setEnabled(false);
        audioImageButton.setEnabled(false);

        callingContext = ((AzureCalling) getApplication()).getCallingContext();
        permissionHelper = ((AzureCalling) getApplication()).getPermissionHelper();

        setVideoImageButtonEnabledState();

        participantViewList = new ArrayList<>();
        participantIdIndexPathMap = new HashMap<>();

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        /* initialize execution control of participant views update */
        initializeDisplayedParticipantsLiveData();

        /* get Join Call Config */
        final JoinCallConfig joinCallConfig = (JoinCallConfig) getIntent()
                .getSerializableExtra(Constants.JOIN_CALL_CONFIG);
        setLayoutComponentState(joinCallConfig.isMicrophoneMuted(), joinCallConfig.isCameraOn(),
                this.callHangUpOverlaid);

        // if the app is already in landscape mode, this check will hide status bar
        setStatusBarVisibility();

        callingContext.joinCallAsync(joinCallConfig).whenComplete((aVoid, throwable) -> {
            runOnUiThread(() -> {
                if (throwable != null) {
                    showCouldNotJoinAlertDialog(throwable.getMessage());
                }
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
            handler.postDelayed(() -> {
                viewUpdatePending = false;
                updateParticipantViews();
                updateParticipantNotificationCount();
                refreshParticipantList();
            }, MIN_TIME_BETWEEN_PARTICIPANT_VIEW_UPDATES);
        });
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setStatusBarVisibility();
        setupScreenLayout();
        setVideoImageButtonEnabledState();
        setLayoutComponentState(!callingContext.getMicOn(), callingContext.getCameraOn(),
                this.callHangUpOverlaid);
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
            if (initialVideoToggleRequest == null) {
                initialVideoToggleRequest = permissionHelper.createVideoPermissionRequest(this,
                        this::onInitialVideoToggleRequest);
            }
            videoImageButton.setEnabled(true);
        } else {
            videoImageButton.setEnabled(true);
        }
    }

    private void setStatusBarVisibility() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            final View decorView = getWindow().getDecorView();
            // Hide Status Bar.
            final int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            final View decorView = getWindow().getDecorView();
            // Show Status Bar.
            final int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
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
        callingContext.pauseVideo().thenRun(() -> runOnUiThread(() ->
                localParticipantView.setVideoStream((LocalVideoStream) null)));
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "CallActivity onResume");
        callingContext.resumeVideo().thenAccept(localVideoStream -> runOnUiThread(() ->
                localParticipantView.setVideoStream(localVideoStream)));
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
        localParticipantView = new LocalParticipantView(this);
        localParticipantView.setDisplayName(callingContext.getDisplayName() + " (Me)");
        localParticipantView.setVideoDisplayed(callingContext.getCameraOn());
        localParticipantView.setSwitchCameraButtonDisplayed(callingContext.getCameraOn());
        localParticipantView.setIsMuted(!callingContext.getMicOn());
        localParticipantView.setSwitchCameraButtonOnClickAction(this::switchCamera);

        if (callingContext.getCameraOn()) {
            callingContext.getLocalVideoStreamCompletableFuture().thenAccept((localVideoStream -> {
                runOnUiThread(() -> localParticipantView.setVideoStream(localVideoStream));
            }));
        } else {
            localParticipantView.setVideoStream((LocalVideoStream) null);
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
        final List<ParticipantView> prevParticipantViewList = participantViewList;
        final Map<String, Integer> preParticipantIdIndexPathMap = participantIdIndexPathMap;

        participantViewList = new ArrayList<>();
        participantIdIndexPathMap = new HashMap<>();

        final List<RemoteParticipant> displayedRemoteParticipants = getRemoteParticipantsToDisplay();
        int indexForNewParticipantViewList = 0;
        for (int i = 0; i < displayedRemoteParticipants.size(); i++) {
            final RemoteParticipant remoteParticipant = displayedRemoteParticipants.get(i);
            final ParticipantState participantState = remoteParticipant.getState();
            if (ParticipantState.DISCONNECTED == participantState) {
                continue;
            }
            final String id = callingContext.getId(remoteParticipant);
            final ParticipantView pv;
            if (preParticipantIdIndexPathMap.containsKey(id)) {
                final int prevIndex = preParticipantIdIndexPathMap.get(id);
                pv = prevParticipantViewList.get(prevIndex);
                final RemoteVideoStream remoteVideoStream = getVideoStream(remoteParticipant);
                pv.setVideoStream(remoteVideoStream);
            } else {
                pv = new ParticipantView(this);
                pv.setDisplayName(remoteParticipant.getDisplayName());
                final RemoteVideoStream remoteVideoStream = getVideoStream(remoteParticipant);
                pv.setVideoStream(remoteVideoStream);
            }

            pv.setIsMuted(remoteParticipant.isMuted());
            pv.setIsSpeaking(remoteParticipant.isSpeaking());

            // update the participantIdIndexPathMap, participantViewList and participantsRenderList
            participantIdIndexPathMap.put(id, indexForNewParticipantViewList++);
            participantViewList.add(pv);
        }

        for (final String id : preParticipantIdIndexPathMap.keySet()) {
            if (participantIdIndexPathMap.containsKey(id)) {
                continue;
            }
            final ParticipantView discardedParticipantView =
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
            final int preSize = prevParticipantViewList.size();
            final int currSize = participantViewList.size();
            if (!rangeInDefined(preSize, 0, 1) && rangeInDefined(currSize, 0, 1)
                || (!rangeInDefined(preSize, 2, 4) && rangeInDefined(currSize, 2, 4))
                || (!rangeInDefined(preSize, 5, 6)) && rangeInDefined(currSize, 5, 6)) {
                setupGridLayout();
            }
            updateGridLayoutViews();
        });
    }

    private List<RemoteParticipant> getRemoteParticipantsToDisplay() {
        final RemoteParticipant currentScreenSharingParticipant =
                callingContext.getCurrentScreenSharingParticipant();
        if (currentScreenSharingParticipant == null) {
            return callingContext.getDisplayedParticipantsLiveData().getValue();
        }
        final List<RemoteParticipant> remoteParticipantList = new ArrayList<>();
        remoteParticipantList.add(currentScreenSharingParticipant);
        return remoteParticipantList;
    }

    private boolean rangeInDefined(final int current, final int min, final int max) {
        return Math.max(min, current) == Math.min(current, max);
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

    private void setParticipantCountToFloatingHeader(final Integer text) {
        runOnUiThread(() ->
                ((TextView) findViewById(R.id.participantCountTextView)).setText(Integer.toString(text + 1)));
    }

    private void setFloatingHeaderVisibility(final int visibility) {
        runOnUiThread(() -> infoHeaderView.setVisibility(visibility));
    }

    private void openShareDialogue() {
        Log.d(LOG_TAG, "Share button clicked!");
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, callingContext.getJoinId());
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Group Call ID");
        sendIntent.setType("text/plain");
        final Intent shareIntent = Intent.createChooser(sendIntent, null);
        shareIntent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(shareIntent);
    }

    private void setLayoutComponentState(
            final boolean isMicrophoneMuted, final boolean isCameraOn,
            final boolean isCallHangUpOverLaid) {
        audioImageButton.setSelected(!isMicrophoneMuted);
        videoImageButton.setSelected(isCameraOn);
        callHangupOverlay.setVisibility(isCallHangUpOverLaid ? View.VISIBLE : View.INVISIBLE);
    }

    private void openAudioDeviceList() {
        if (audioDeviceSelectionPopupWindow == null) {
            final AudioSessionManager audioSessionManager
                    = ((AzureCalling) getApplicationContext()).getAudioSessionManager();
            audioDeviceSelectionPopupWindow = new AudioDeviceSelectionPopupWindow(this, audioSessionManager);
        }
        audioDeviceSelectionPopupWindow.showAtLocation(getWindow().getDecorView().getRootView(),
                Gravity.BOTTOM, 0, 0);
    }

    private void openParticipantList() {
        if (participantListPopupWindow == null) {
            participantListPopupWindow = new ParticipantListPopupWindow(this, Collections.emptyList());
        }
        refreshParticipantList();
        participantListPopupWindow.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.BOTTOM, 0, 0);
    }

    private void refreshParticipantList() {
        final List<ParticipantInfo> participantInfo = new ArrayList<>();
        participantInfo.add(new ParticipantInfo(callingContext.getDisplayName(), !callingContext.getMicOn()));
        callingContext.getRemoteParticipants().stream().forEach(remoteParticipant ->
                participantInfo.add(new ParticipantInfo(remoteParticipant.getDisplayName(),
                        remoteParticipant.isMuted())));

        if (participantListPopupWindow == null) {
            participantListPopupWindow = new ParticipantListPopupWindow(this, participantInfo);
        }

        participantListPopupWindow.setParticipantInfo(participantInfo);
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
        endCall();
        final Intent intent = new Intent(this, EndActivity.class);
        startActivity(intent);
        finish();
    }

    private void endCall() {
        callingContext.getDisplayedParticipantsLiveData().removeObservers(this);
        if (localParticipantView != null) {
            localParticipantView.cleanUpVideoRendering();
            detachFromParentView(localParticipantView);
        }
        stopService(inCallServiceIntent);
        callHangupConfirmButton.setEnabled(false);
        callingContext.hangupAsync();
    }

    private void toggleVideo(final boolean isSelected) {
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
        final PermissionState videoAccess = permissionHelper.getVideoPermissionState(this);
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
        callingContext.turnOnVideoAsync().thenAccept(localVideoStream -> {
            runOnUiThread(() -> {
                localParticipantView.setVideoStream(localVideoStream);
                localParticipantView.setVideoDisplayed(callingContext.getCameraOn());
                localParticipantView.setSwitchCameraButtonDisplayed(callingContext.getCameraOn());
                localVideoViewContainer.setVisibility(
                        (localParticipantViewGridIndex == null && !callingContext.getCameraOn())
                                ? View.INVISIBLE : View.VISIBLE);
                videoImageButton.setSelected(true);
            });
        });
    }

    private void toggleVideoOff() {
        Log.d(LOG_TAG, "toggleVideo -> off");
        callingContext.turnOffVideoAsync().thenRun(() -> {
            runOnUiThread(() -> {
                localParticipantView.setVideoStream((LocalVideoStream) null);
                localParticipantView.setVideoDisplayed(callingContext.getCameraOn());
                localParticipantView.setSwitchCameraButtonDisplayed(callingContext.getCameraOn());
                localVideoViewContainer.setVisibility(
                        (localParticipantViewGridIndex == null && !callingContext.getCameraOn())
                                ? View.INVISIBLE : View.VISIBLE);
                videoImageButton.setSelected(false);
            });
        });
    }

    private void switchCamera() {
        callingContext.switchCameraAsync().thenRun(() -> {
            runOnUiThread(() -> localParticipantView.setSwitchCameraButtonEnabled(true));
        });
    }

    private void toggleAudio(final boolean toggleOff) {
        if (!toggleOff) {
            callingContext.turnOnAudioAsync().whenComplete((aVoid, throwable) -> {
                runOnUiThread(() -> {
                    audioImageButton.setSelected(true);
                    localParticipantView.setIsMuted(false);
                });
            });
        } else {
            callingContext.turnOffAudioAsync().whenComplete((aVoid, throwable) -> {
                runOnUiThread(() -> {
                    audioImageButton.setSelected(false);
                    localParticipantView.setIsMuted(true);
                });
            });
        }
    }

    private void detachFromParentView(final View view) {
        if (view != null && view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private RemoteVideoStream getVideoStream(final RemoteParticipant remoteParticipant) {
        final List<RemoteVideoStream> remoteVideoStreams = remoteParticipant.getVideoStreams();
        if (callingContext.getCurrentScreenSharingParticipant() != null) {
            for (final RemoteVideoStream videoStream : remoteVideoStreams) {
                if (videoStream.getMediaStreamType() == MediaStreamType.SCREEN_SHARING) {
                    return videoStream;
                }
            }
        } else if (remoteVideoStreams.size() > 0) {
            return remoteVideoStreams.get(0);
        }
        return null;
    }

    private void setupScreenLayout() {
        setContentView(R.layout.activity_call);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        videoImageButton = findViewById(R.id.call_video);
        videoImageButton.setOnClickListener(l -> toggleVideo(videoImageButton.isSelected()));

        audioImageButton = findViewById(R.id.call_audio);
        audioImageButton.setOnClickListener(l -> toggleAudio(audioImageButton.isSelected()));

        final ImageButton shareButton = findViewById(R.id.call_share);
        shareButton.setOnClickListener(l -> openShareDialogue());

        final ImageButton hangupButton = findViewById(R.id.call_hangup);
        hangupButton.setOnClickListener(l -> openHangupDialog());

        callHangupConfirmButton = findViewById(R.id.call_hangup_confirm);
        callHangupConfirmButton.setOnClickListener(l -> hangup());

        final Button callHangupCancelButton = findViewById(R.id.call_hangup_cancel);
        callHangupCancelButton.setOnClickListener(l -> closeHangupDialog());

        final ImageButton deviceOptionsButton = findViewById(R.id.audio_device_button);
        deviceOptionsButton.setOnClickListener(l -> openAudioDeviceList());

        infoHeaderView = findViewById(R.id.info_header);
        gridLayout = findViewById(R.id.groupCallTable);
        localVideoViewContainer = findViewById(R.id.yourCameraHolder);

        gridLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                toggleParticipantHeaderNotification();
                return true;
            }
            return false;
        });
        final ImageButton participantListButton = findViewById(R.id.participant_drawer_button);
        participantListButton.setOnClickListener(l -> openParticipantList());

        callHangupOverlay = findViewById(R.id.call_hangup_overlay);
        callHangupOverlay.setOnTouchListener((v, event) -> {
            closeHangupDialog();
            return true;
        });
    }

    private void setupGridLayout() {
        gridLayout.removeAllViews();
        final int size = participantViewList.size();
        final int width;
        final int height;
        final int range;
        final int rowCount;
        final int colCount;
        if (size <= 1) {
            width = gridLayout.getMeasuredWidth();
            height = gridLayout.getMeasuredHeight();
            range = 1;
            rowCount = 1;
            colCount = 1;
        } else if (size > 1 && size <= 4) {
            width = gridLayout.getMeasuredWidth() / 2;
            height = gridLayout.getMeasuredHeight() / 2;
            range = 4;
            rowCount = 2;
            colCount = 2;
        } else {
            final boolean isLandscape = getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
            range = 6;
            if (isLandscape) {
                width = gridLayout.getMeasuredWidth() / 3;
                height = gridLayout.getMeasuredHeight() / 2;
                rowCount = 2;
                colCount = 3;
            } else {
                width = gridLayout.getMeasuredWidth() / 2;
                height = gridLayout.getMeasuredHeight() / 3;
                rowCount = 3;
                colCount = 2;
            }
        }
        gridLayout.setRowCount(rowCount);
        gridLayout.setColumnCount(colCount);
        for (int i = 0; i < range; i++) {
            gridLayout.addView(createCellForGridLayout(width, height));
        }
    }

    private void loadGridLayoutViews() {
        setupGridLayout();
        for (int i = 0; i < participantViewList.size(); i++) {
            final ParticipantView participantView = participantViewList.get(i);
            detachFromParentView(participantView);
            ((LinearLayout) gridLayout.getChildAt(i)).addView(participantView, 0);
        }
    }

    private void setLocalParticipantView() {
        detachFromParentView(localParticipantView);
        localVideoViewContainer.setVisibility(callingContext.getCameraOn() ? View.VISIBLE : View.INVISIBLE);
        localParticipantView.setDisplayNameVisible(false);
        localParticipantView.centerSwitchCameraButton(false);
        localVideoViewContainer.addView(localParticipantView);
        localVideoViewContainer.bringToFront();
    }

    private void appendLocalParticipantView() {
        localParticipantViewGridIndex = participantViewList.size();
        localParticipantView.setDisplayNameVisible(true);
        localParticipantView.setVideoDisplayed(callingContext.getCameraOn());
        localParticipantView.setSwitchCameraButtonDisplayed(callingContext.getCameraOn());
        localParticipantView.centerSwitchCameraButton(true);
        participantViewList.add(localParticipantView);
        localVideoViewContainer.setVisibility(View.INVISIBLE);
    }

    private void showCouldNotJoinAlertDialog(final String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sorry, we could not join the meeting due to the following error: " + message)
                .setCancelable(false)
                .setPositiveButton("Dismiss", (dialog, result) -> {
                    finish();
                });
        builder.create().show();
    }

    private void updateGridLayoutViews() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final LinearLayout wrap = (LinearLayout) gridLayout.getChildAt(i);
            final ParticipantView preParticipantView = (ParticipantView) wrap.getChildAt(0);

            if (i >= participantViewList.size()) {
                wrap.removeAllViews();
            } else {
                final ParticipantView newParticipantView = participantViewList.get(i);
                if (preParticipantView != newParticipantView) {
                    detachFromParentView(newParticipantView);
                    wrap.removeAllViews();
                    wrap.addView(newParticipantView, 0);
                }
            }
        }
    }

    private LinearLayout createCellForGridLayout(final int width, final int height) {
        final LinearLayout cell = new LinearLayout(this);
        final GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = width;
        params.height = height;
        cell.setLayoutParams(params);
        return cell;
    }
}
