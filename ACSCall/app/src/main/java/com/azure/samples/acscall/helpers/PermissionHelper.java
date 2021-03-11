// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.acscall.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

public class PermissionHelper {
    public static final String[] AUDIO_PERMISSIONS = new String[] {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.FOREGROUND_SERVICE };

    public Runnable createAudioPermissionRequest(ActivityResultCaller activityResultCaller, Runnable callback) {
        ActivityResultLauncher<String[]> launcher = activityResultCaller.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), resultsMap -> callback.run());

        return () -> launcher.launch(AUDIO_PERMISSIONS);
    }

    public Runnable createVideoPermissionRequest(ActivityResultCaller activityResultCaller, Runnable callback) {
        ActivityResultLauncher<String> launcher = activityResultCaller.registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(), permissionResult -> callback.run());

        return () -> launcher.launch(Manifest.permission.CAMERA);
    }

    public PermissionState getAudioPermissionState(Activity activity) {
        boolean audioAccess = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED);
        boolean isAudioPermissionPreviouslyDenied
                = activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);

        if (audioAccess) {
            return PermissionState.GRANTED;
        } else if (isAudioPermissionPreviouslyDenied) {
            return PermissionState.DENIED;
        } else {
            return PermissionState.NOT_ASKED;
        }
    }

    public PermissionState getVideoPermissionState(Activity activity) {
        boolean videoAccess = (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);
        boolean isVideoPermissionPreviouslyDenied
                = activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);

        if (videoAccess) {
            return PermissionState.GRANTED;
        } else if (isVideoPermissionPreviouslyDenied) {
            return PermissionState.DENIED;
        } else {
            return PermissionState.NOT_ASKED;
        }

    }
}


