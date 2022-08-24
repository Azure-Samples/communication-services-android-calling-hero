// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.fragment.app.Fragment;
import com.azure.android.communication.ui.calling.CallCompositeEventHandler;
import com.azure.android.communication.ui.calling.models.CallCompositeErrorCode;
import com.azure.android.communication.ui.calling.models.CallCompositeErrorEvent;
import com.azure.samples.communication.calling.contracts.Constants;
import com.azure.samples.communication.calling.contracts.SampleErrorMessages;
import com.azure.samples.communication.calling.views.components.ErrorInfoBar;

public abstract class AbstractBaseFragment extends Fragment {
    protected CallCompositeEventHandler<CallCompositeErrorEvent> callCompositeEventHandler =
            new CallCompositeEventHandler<CallCompositeErrorEvent>() {
        @Override
        public void handle(final CallCompositeErrorEvent eventArgs) {
            if (eventArgs.getErrorCode().equals(CallCompositeErrorCode.CALL_JOIN_FAILED)) {
                showError(SampleErrorMessages.CALL_COMPOSITE_JOIN_CALL_FAILED);
            } else if (eventArgs.getErrorCode().equals(CallCompositeErrorCode.CALL_END_FAILED)) {
                showError(SampleErrorMessages.CALL_COMPOSITE_END_CALL_FAILED);
            } else if (eventArgs.getErrorCode().equals(CallCompositeErrorCode.TOKEN_EXPIRED)) {
                showError(SampleErrorMessages.CALL_COMPOSITE_TOKEN_EXPIRED);
            }
        }
    };
    protected SharedPreferences getSharedPreferences() {
        return requireActivity().getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
    }
    protected void showError(final String error) {
        new ErrorInfoBar().displayErrorInfoBar(this.getView(), error);
    }
}
