package com.azure.samples.communication.ui.calling.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.azure.samples.communication.ui.calling.R;
import com.azure.samples.communication.ui.calling.views.activities.Constants;
import com.microsoft.fluentui.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GroupMeetingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EditText groupMeetingID;
    private EditText displayNameEditor;
    private Button joinCallButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GroupMeetingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sharedPreferences = this.getActivity().getSharedPreferences(Constants.ACS_SHARED_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_group_meeting, container, false);
        joinCallButton = inflatedView.findViewById(R.id.composite_setup_next_button);
        joinCallButton.setOnClickListener(l -> joinCall());

        displayNameEditor = inflatedView.findViewById(R.id.group_call_display_name);
        final String savedDisplayName = sharedPreferences.getString(Constants.ACS_DISPLAY_NAME, "");
        if(savedDisplayName.length() > 0) {
            displayNameEditor.setText(savedDisplayName);
        }

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



    private void joinCall() {
        final String displayName = displayNameEditor.getText().toString();
        if(!checkValidity(displayName)) {
            // Show error card
            return ;
        }

        final String groupCallId = groupMeetingID.getText().toString();
        groupCallId.trim();
        groupMeetingID.setText(groupCallId);

        editor.putString(Constants.ACS_DISPLAY_NAME, displayName);
        editor.putString(Constants.ACS_MEETING_ID, groupCallId);
        editor.commit();

    }

    private boolean checkValidity(final String displayName) {
        if(displayName.length() == 0) return false;

        for(char c: displayName.toCharArray()) {
            if(c != ' ')return true;
        }
        return false;
    }
}