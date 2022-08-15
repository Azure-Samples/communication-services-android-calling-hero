package com.azure.samples.communication.ui.calling.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azure.samples.communication.ui.calling.R;

/**
 * Fragment to host teams call meetings
 */
public class TeamsMeetingFragment extends Fragment {

    public TeamsMeetingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_teams_meeting, container, false);
    }
}