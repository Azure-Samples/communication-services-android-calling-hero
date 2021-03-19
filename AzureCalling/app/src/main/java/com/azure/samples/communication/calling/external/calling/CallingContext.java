// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.external.calling;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.azure.android.communication.calling.AudioOptions;
import com.azure.android.communication.calling.Call;
import com.azure.android.communication.calling.CallAgent;
import com.azure.android.communication.calling.CallAgentOptions;
import com.azure.android.communication.calling.CallClient;
import com.azure.android.communication.calling.DeviceManager;
import com.azure.android.communication.calling.GroupCallLocator;
import com.azure.android.communication.calling.HangUpOptions;
import com.azure.android.communication.calling.JoinCallOptions;
import com.azure.android.communication.calling.LocalVideoStream;
import com.azure.android.communication.calling.ParticipantsUpdatedEvent;
import com.azure.android.communication.calling.PropertyChangedListener;
import com.azure.android.communication.calling.RemoteParticipant;
import com.azure.android.communication.calling.RemoteVideoStreamsUpdatedListener;
import com.azure.android.communication.calling.VideoDeviceInfo;
import com.azure.android.communication.calling.VideoOptions;
import com.azure.android.communication.common.CommunicationIdentifier;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.communication.common.CommunicationTokenRefreshOptions;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.android.communication.common.PhoneNumberIdentifier;
import com.azure.android.communication.common.UnknownIdentifier;
import com.azure.samples.communication.calling.helpers.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import java9.util.concurrent.CompletableFuture;

public class CallingContext {
    //region Constants
    private static final String LOG_TAG = CallingContext.class.getSimpleName();
    //endregion

    //region Properties
    Context appContext;

    private final Callable<String> tokenFetcher;
    private String groupId;
    private CallClient callClient;
    private Call call;
    private String displayName;
    private CompletableFuture<CommunicationTokenCredential> communicationUserCredentialCompletableFuture;
    private CompletableFuture<CallAgent> callAgentCompletableFuture;
    private CompletableFuture<DeviceManager> deviceManagerCompletableFuture;
    private CompletableFuture<VideoDeviceInfo> initializeCameraCompletableFuture;
    private CompletableFuture<LocalVideoStream> localVideoStreamCompletableFuture;
    private CompletableFuture<Void> setupCompletableFuture;

    // Meeting State
    private boolean cameraOn;
    private boolean micOn;
    private boolean isVideoOnHold = false;

    private final List<RemoteParticipant> remoteParticipants;
    private final List<RemoteParticipant> displayedRemoteParticipants;
    private final MutableLiveData<List<RemoteParticipant>> displayedParticipantsLiveData;
    private final Set<String> displayedRemoteParticipantIds;

    private final Map<String, RemoteVideoStreamsUpdatedListener> videoStreamsUpdatedListenersMap;
    private final Map<String, PropertyChangedListener> mutedChangedListenersMap;
    private final Map<String, PropertyChangedListener> isSpeakingChangedListenerMap;
    private final Map<String, PropertyChangedListener> participantStateChangedListenerMap;
    //endregion


    //region Constructors
    public CallingContext(final Context applicationContext, final Callable<String> tokenFetcher) {
        Log.d(LOG_TAG, "Creating CallingContext");
        this.tokenFetcher = tokenFetcher;
        appContext = applicationContext;
        remoteParticipants = new ArrayList<>();
        displayedRemoteParticipants = new ArrayList<>();
        displayedParticipantsLiveData = new MutableLiveData<>();
        displayedRemoteParticipantIds = new HashSet<>();
        videoStreamsUpdatedListenersMap = new HashMap<>();
        mutedChangedListenersMap = new HashMap<>();
        isSpeakingChangedListenerMap = new HashMap<>();
        participantStateChangedListenerMap = new HashMap<>();
    }

    //endregion

    //region Public Methods
    public CompletableFuture<Void> setupAsync() {
        // Initialize CompletableFutures
        setupCompletableFuture = new CompletableFuture<>();
        communicationUserCredentialCompletableFuture = new CompletableFuture<>();
        callAgentCompletableFuture = new CompletableFuture<>();
        deviceManagerCompletableFuture = new CompletableFuture<>();
        initializeCameraCompletableFuture = new CompletableFuture<>();

        // Define completion code for setup
        createCallClient();
        createTokenCredential();
        createCallAgent(null);
        createDeviceManager();
        initializeCamera();
        initializeSpeaker();

        // Wait until everything except localVideoStream is ready to define setup ready
        CompletableFuture.allOf(
                communicationUserCredentialCompletableFuture,
                callAgentCompletableFuture,
                deviceManagerCompletableFuture,
                initializeCameraCompletableFuture).whenComplete((aVoid, throwable) -> {
                    setupCompletableFuture.complete(aVoid);
                }
        );

        return setupCompletableFuture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CompletableFuture<Void> getSetupCompletableFuture() {
        return setupCompletableFuture;
    }

    public CompletableFuture<LocalVideoStream> getLocalVideoStreamCompletableFuture() {
        if (localVideoStreamCompletableFuture == null) {
            localVideoStreamCompletableFuture = createLocalVideoStreamAsync();
        }
        return localVideoStreamCompletableFuture;
    }

    public boolean getCameraOn() {
        return cameraOn;
    }

    public boolean getMicOn() {
        return micOn;
    }

    public void createCallClient() {
        callClient = new CallClient();
    }

    public String getGroupId() {
        return groupId;
    }

    /**
     * Join a call
     */
    public CompletableFuture<Void> joinCallAsync(final JoinCallConfig joinCallConfig) {
        // Recreate Token and Call Agent as a work-around
        communicationUserCredentialCompletableFuture = new CompletableFuture<>();
        callAgentCompletableFuture = new CompletableFuture<>();
        createTokenCredential();
        createCallAgent(joinCallConfig.getDisplayName());

        if (joinCallConfig.getGroupId() == null) {
            groupId = UUID.randomUUID().toString();
        } else {
            groupId = joinCallConfig.getGroupId();
        }
        final GroupCallLocator groupCallLocator = new GroupCallLocator(UUID.fromString(groupId));

        final AudioOptions audioOptions = new AudioOptions();
        audioOptions.setMuted(joinCallConfig.isMicrophoneMuted());


        return callAgentCompletableFuture.thenAccept(agent -> {
            if (joinCallConfig.isCameraOn()) {
                localVideoStreamCompletableFuture.thenAccept(localVideoStream -> {
                    final VideoOptions videoOptions = new VideoOptions(localVideoStream);
                    callWithOptions(agent, audioOptions, videoOptions, groupCallLocator);
                });
            } else {
                callWithOptions(agent, audioOptions, null, groupCallLocator);
            }
        });
    }

    public CompletableFuture hangupAsync() {
        call.removeOnRemoteParticipantsUpdatedListener(this::onParticipantsUpdated);
        return call.hangUp(new HangUpOptions());
    }

    public CompletableFuture<LocalVideoStream> turnOnVideoAsync() {
        if (call == null) {
            throw new IllegalStateException("Call can't be null");
        }

        return getLocalVideoStreamCompletableFuture().thenCompose(localVideoStream ->
                call.startVideo(localVideoStream).thenApply(nothing -> {
                    cameraOn = true;
                    return localVideoStream;
                }));
    }

    public CompletableFuture turnOffVideoAsync() {
        if (call == null) {
            throw new IllegalStateException("Call can't be null");
        }

        return getLocalVideoStreamCompletableFuture().thenCompose(localVideoStream ->
                call.stopVideo(localVideoStream).thenRun(() -> cameraOn = false));
    }

    public void pauseVideo() {
        if (cameraOn && call != null) {
            turnOffVideoAsync().thenRun(() -> {
                isVideoOnHold = true;
            });
        }
    }

    public void resumeVideo() {
        if (isVideoOnHold && call != null) {
            turnOnVideoAsync().thenRun(() -> {
                isVideoOnHold = false;
            });
        }
    }

    public CompletableFuture turnOnAudioAsync() {
        if (call == null) {
            throw new IllegalStateException("Call can't be null");
        }

        return call.unmute().thenRun(() -> micOn = true);
    }

    public CompletableFuture turnOffAudioAsync() {
        if (call == null) {
            throw new IllegalStateException("Call can't be null");
        }

        return call.mute().thenRun(() -> micOn = false);
    }

    public int getRemoteParticipantCount() {
        return remoteParticipants.size();
    }

    public MutableLiveData<List<RemoteParticipant>> getDisplayedParticipantsLiveData() {
        return displayedParticipantsLiveData;
    }

    //endregion
    //region Private Methods

    private void createTokenCredential() {
        final CommunicationTokenCredential credential = new CommunicationTokenCredential(
                new CommunicationTokenRefreshOptions(tokenFetcher, true));
        communicationUserCredentialCompletableFuture.complete(credential);
    }

    private void createDeviceManager() {
        callAgentCompletableFuture.whenComplete((callAgent, throwable) -> {
            Log.d(LOG_TAG, "Call Agent created");
            callClient.getDeviceManager().thenAccept(deviceManager -> {
                deviceManagerCompletableFuture.complete(deviceManager);
            });
        });
    }

    private void initializeCamera() {
        deviceManagerCompletableFuture.whenComplete((deviceManager, throwable) -> {
            Log.d(LOG_TAG, "Device Manager created");

            List<VideoDeviceInfo> cameras;
            boolean cameraFound = false;
            while (!cameraFound) {
                cameras = deviceManager.getCameras();
                for (final VideoDeviceInfo camera: cameras) {
                    final String cameraFacingName = camera.getCameraFacing().name();

                    if (cameraFacingName.equalsIgnoreCase("front")) {
                        Log.i(LOG_TAG, "Desired Camera selected");
                        initializeCameraCompletableFuture.complete(camera);
                        cameraFound = true;
                        break;
                    }
                }
            }
        });
    }

    private void initializeSpeaker() {
        final AudioManager audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
    }

    private void createCallAgent(final String userName) {
        communicationUserCredentialCompletableFuture.whenComplete((communicationUserCredential, throwable) -> {
            final CompletableFuture<CallAgent> completableFuture;
            if (userName != null) {
                displayName = userName;
                final CallAgentOptions options = new CallAgentOptions();
                options.setDisplayName(userName);
                completableFuture = callClient.createCallAgent(appContext, communicationUserCredential, options);
            } else {
                completableFuture = callClient.createCallAgent(appContext, communicationUserCredential);
            }
            completableFuture.whenComplete((callAgent, callAgentThrowable) -> {
                callAgentCompletableFuture.complete(callAgent);
            });
        });
    }

    private CompletableFuture<LocalVideoStream> createLocalVideoStreamAsync() {
        return initializeCameraCompletableFuture.thenApply(desiredCamera ->
                new LocalVideoStream(desiredCamera, appContext));
    }

    private void callWithOptions(
            final CallAgent agent,
            final AudioOptions audioOptions,
            final VideoOptions videoOptions,
            final GroupCallLocator groupCallLocator) {
        final JoinCallOptions joinCallOptions = new JoinCallOptions();
        joinCallOptions.setVideoOptions(videoOptions);
        joinCallOptions.setAudioOptions(audioOptions);
        call = agent.join(appContext, groupCallLocator, joinCallOptions);
        Log.d(LOG_TAG, "Call ID: " + groupId);

        call.addOnRemoteParticipantsUpdatedListener(this::onParticipantsUpdated);

        cameraOn = (videoOptions != null);
        micOn = !audioOptions.isMuted();
    }

    private boolean addParticipants(final List<RemoteParticipant> addedParticipants) {
        boolean isParticipantsAddedToDisplayedRemoteParticipants = false;

        for (final RemoteParticipant addedParticipant: addedParticipants) {
            remoteParticipants.add(addedParticipant);
            bindOnVideoStreamsUpdatedListener(addedParticipant);
            bindOnIsMutedChangedListener(addedParticipant);
            bindOnIsSpeakingChangedListener(addedParticipant);
            bindOnParticipantStateChangedListener(addedParticipant);
        }

        if (remoteParticipants.size() > displayedRemoteParticipants.size()) {
            for (final RemoteParticipant remoteParticipant : remoteParticipants) {
                if (displayedRemoteParticipants.size() == Constants.DISPLAYED_REMOTE_PARTICIPANT_SIZE_LIMIT) {
                    break;
                }
                final String id = getId(remoteParticipant);
                if (!displayedRemoteParticipantIds.contains(id)) {
                    displayedRemoteParticipants.add(remoteParticipant);
                    displayedRemoteParticipantIds.add(id);
                    isParticipantsAddedToDisplayedRemoteParticipants = true;
                }
            }
        }

        return isParticipantsAddedToDisplayedRemoteParticipants;
    }

    private boolean removeParticipants(final List<RemoteParticipant> removedParticipants) {
        boolean isDisplayedRemoteParticipantsChanged = false;
        for (final RemoteParticipant removedParticipant: removedParticipants) {
            final String removedParticipantId = getId(removedParticipant);

            unbindOnVideoStreamsUpdatedListener(removedParticipant);
            unbindOnIsMutedChangedListener(removedParticipant);
            unbindOnIsSpeakingChangedListener(removedParticipant);
            unbindOnParticipantStateChangedListener(removedParticipant);

            int indexTobeRemovedForRemoteParticipants = -1;
            for (int i = 0; i < remoteParticipants.size(); i++) {
                final String currentRemoteParticipantId = getId(remoteParticipants.get(i));
                if (currentRemoteParticipantId.equals(removedParticipantId)) {
                    indexTobeRemovedForRemoteParticipants = i;
                    break;
                }
            }
            remoteParticipants.remove(indexTobeRemovedForRemoteParticipants);

            if (displayedRemoteParticipantIds.contains(removedParticipantId)) {
                int indexTobeRmovedForDisplayedRemoteParticipants = -1;
                for (int i = 0; i < displayedRemoteParticipants.size(); i++) {
                    final String currentDisplayedRemoteParticipantId = getId(displayedRemoteParticipants.get(i));
                    if (currentDisplayedRemoteParticipantId.equals(removedParticipantId)) {
                        indexTobeRmovedForDisplayedRemoteParticipants = i;
                        break;
                    }
                }
                if (indexTobeRmovedForDisplayedRemoteParticipants != -1) {
                    displayedRemoteParticipants.remove(indexTobeRmovedForDisplayedRemoteParticipants);
                    displayedRemoteParticipantIds.remove(removedParticipantId);
                    isDisplayedRemoteParticipantsChanged = true;
                }
            }
        }
        if (remoteParticipants.size() > displayedRemoteParticipants.size()) {
            for (final RemoteParticipant remoteParticipant : remoteParticipants) {
                if (displayedRemoteParticipants.size() == Constants.DISPLAYED_REMOTE_PARTICIPANT_SIZE_LIMIT) {
                    break;
                }
                final String id = getId(remoteParticipant);
                if (!displayedRemoteParticipantIds.contains(id)) {
                    displayedRemoteParticipants.add(remoteParticipant);
                    displayedRemoteParticipantIds.add(id);
                    isDisplayedRemoteParticipantsChanged = true;
                }
            }
        }
        return isDisplayedRemoteParticipantsChanged;
    }


    private void onParticipantsUpdated(final ParticipantsUpdatedEvent participantsUpdatedEvent) {
        boolean doUpdate = false;
        if (!participantsUpdatedEvent.getRemovedParticipants().isEmpty()) {
            doUpdate |= removeParticipants(participantsUpdatedEvent.getRemovedParticipants());
        }
        if (!participantsUpdatedEvent.getAddedParticipants().isEmpty()) {
            doUpdate |= addParticipants(participantsUpdatedEvent.getAddedParticipants());
        }
        if (doUpdate) {
            displayedParticipantsLiveData.postValue(displayedRemoteParticipants);
        }
    }

    private void bindOnVideoStreamsUpdatedListener(final RemoteParticipant remoteParticipant) {
        final String username = remoteParticipant.getDisplayName();
        final String id = getId(remoteParticipant);
        final RemoteVideoStreamsUpdatedListener remoteVideoStreamsUpdatedListener = remoteVideoStreamsEvent -> {
            if (!displayedRemoteParticipantIds.contains(id)) {
                return;
            }
            displayedParticipantsLiveData.postValue(displayedRemoteParticipants);
            Log.d(LOG_TAG, String.format("Remote Participant %s addOnRemoteVideoStreamsUpdatedListener", username));
        };
        remoteParticipant.addOnVideoStreamsUpdatedListener(remoteVideoStreamsUpdatedListener);
        videoStreamsUpdatedListenersMap.put(id, remoteVideoStreamsUpdatedListener);
    }

    private void unbindOnVideoStreamsUpdatedListener(final RemoteParticipant remoteParticipant) {
        final String removedParticipantId = getId(remoteParticipant);
        remoteParticipant.removeOnVideoStreamsUpdatedListener(
                videoStreamsUpdatedListenersMap.remove(removedParticipantId));
    }

    private void bindOnIsMutedChangedListener(final RemoteParticipant remoteParticipant) {
        final String username = remoteParticipant.getDisplayName();
        final String id = getId(remoteParticipant);
        final PropertyChangedListener remoteIsMutedChangedListener = propertyChangedEvent ->
                Log.d(LOG_TAG, String.format("Remote Participant %s addOnIsMutedChangedListener called", username));
        remoteParticipant.addOnIsMutedChangedListener(remoteIsMutedChangedListener);
        mutedChangedListenersMap.put(id, remoteIsMutedChangedListener);
    }

    private void unbindOnIsMutedChangedListener(final RemoteParticipant remoteParticipant) {
        final String removedParticipantId = getId(remoteParticipant);
        remoteParticipant.removeOnIsMutedChangedListener(mutedChangedListenersMap.remove(removedParticipantId));
    }

    private void bindOnIsSpeakingChangedListener(final RemoteParticipant remoteParticipant) {
        final String username = remoteParticipant.getDisplayName();
        final String id = getId(remoteParticipant);
        final PropertyChangedListener remoteIsSpeakingChangedListener = propertyChangedEvent -> {
            if (displayedRemoteParticipantIds.contains(id)) {
                return;
            }
            findInactiveSpeakerToSwap(remoteParticipant, id);
            Log.d(LOG_TAG, String.format("Remote Participant %s addOnIsSpeakingChangedListener called", username));
        };
        remoteParticipant.addOnIsSpeakingChangedListener(remoteIsSpeakingChangedListener);
        isSpeakingChangedListenerMap.put(id, remoteIsSpeakingChangedListener);
    }

    private void unbindOnIsSpeakingChangedListener(final RemoteParticipant remoteParticipant) {
        final String removedParticipantId = getId(remoteParticipant);
        remoteParticipant.removeOnIsSpeakingChangedListener(isSpeakingChangedListenerMap.remove(removedParticipantId));
    }

    private void bindOnParticipantStateChangedListener(final RemoteParticipant remoteParticipant) {
        final String username = remoteParticipant.getDisplayName();
        final String id = getId(remoteParticipant);
        final PropertyChangedListener remoteParticipantStateChangedListener = propertyChangedEvent ->
                Log.d(LOG_TAG, String.format(
                        "Remote Participant %s addOnParticipantStateChangedListener called", username));
        remoteParticipant.addOnStateChangedListener(remoteParticipantStateChangedListener);
        participantStateChangedListenerMap.put(id, remoteParticipantStateChangedListener);
    }

    private void unbindOnParticipantStateChangedListener(final RemoteParticipant remoteParticipant) {
        final String removedParticipantId = getId(remoteParticipant);
        remoteParticipant.removeOnStateChangedListener(participantStateChangedListenerMap.remove(removedParticipantId));
    }

    private void findInactiveSpeakerToSwap(final RemoteParticipant remoteParticipant, final String id) {
        for (int i = 0; i < displayedRemoteParticipants.size(); i++) {
            final RemoteParticipant displayedRemoteParticipant = displayedRemoteParticipants.get(i);
            if (!displayedRemoteParticipant.isSpeaking()) {
                final String originId = getId(displayedRemoteParticipant);
                displayedRemoteParticipantIds.remove(originId);
                displayedRemoteParticipants.set(i, remoteParticipant);
                displayedRemoteParticipantIds.add(id);
                displayedParticipantsLiveData.postValue(displayedRemoteParticipants);
                break;
            }
        }
    }

    public String getId(final RemoteParticipant remoteParticipant) {
        final CommunicationIdentifier identifier = remoteParticipant.getIdentifier();
        if (identifier instanceof PhoneNumberIdentifier) {
            return ((PhoneNumberIdentifier) identifier).getPhoneNumber();
        } else if (identifier instanceof MicrosoftTeamsUserIdentifier) {
            return ((MicrosoftTeamsUserIdentifier) identifier).getUserId();
        } else if (identifier instanceof CommunicationUserIdentifier) {
            return ((CommunicationUserIdentifier) identifier).getId();
        } else {
            return ((UnknownIdentifier) identifier).getId();
        }
    }
}

