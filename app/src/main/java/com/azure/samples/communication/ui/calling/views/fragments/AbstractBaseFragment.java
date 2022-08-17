
package com.azure.samples.communication.ui.calling.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import com.azure.samples.communication.ui.calling.contracts.Constants;
import com.azure.samples.communication.ui.calling.contracts.SampleErrorMessages;
import com.azure.samples.communication.ui.calling.views.components.ErrorInfoBar;

public abstract class AbstractBaseFragment extends Fragment {
    protected SharedPreferences getSharedPreferences(){
        return requireActivity().getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
    }
    protected void showError(String error){
        new ErrorInfoBar().displayErrorInfoBar(this.getView(), error);
    }
}
