package com.azure.samples.communication.ui.calling.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.azure.samples.communication.ui.calling.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupMeetingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupMeetingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private EditText groupMeetingID;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GroupMeetingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupMeetingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupMeetingFragment newInstance(String param1, String param2) {
        GroupMeetingFragment fragment = new GroupMeetingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_group_meeting, container, false);

        groupMeetingID = inflatedView.findViewById(R.id.group_call_id);
        groupMeetingID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = charSequence.toString();
                int textLength = text.length();
                if(!Character.isDigit(text.charAt(textLength-1))
                        && !Character.isAlphabetic(text.charAt(textLength-1))
                        && text.charAt(textLength-1) != '-') {
                    // Throw error dialog box
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // Inflate the layout for this fragment
        return inflatedView;
    }
}