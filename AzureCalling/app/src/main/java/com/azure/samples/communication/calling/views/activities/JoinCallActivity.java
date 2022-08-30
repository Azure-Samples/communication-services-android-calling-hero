// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.activities;

import static com.azure.samples.communication.calling.contracts.Constants.JOIN_CALL;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.views.fragments.TeamsMeetingFragment;
import com.azure.samples.communication.calling.views.fragments.GroupMeetingFragment;
import com.google.android.material.tabs.TabLayout;

public class JoinCallActivity extends AppCompatActivity {

    private TabLayout meetingTabLayout;
    private Context context;
    private View groupMeetingTab;
    private View teamsMeetingTab;

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_call);

        final ActionBar ab = getSupportActionBar();
        // Disable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(JOIN_CALL);
        }

        initializeUI();
        if (savedInstanceState == null) {
            showGroupFragment();
        }
    }

    private void initializeUI() {
        context = this;
        meetingTabLayout = findViewById(R.id.meeting_tab_layout);

        groupMeetingTab = ((ViewGroup) meetingTabLayout.getChildAt(0)).getChildAt(0);
        teamsMeetingTab = ((ViewGroup) meetingTabLayout.getChildAt(0)).getChildAt(1);

        meetingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                
                if (tab.getText().equals("Group Meeting")) {
                    showGroupFragment();
                } else {
                    showTeamsFragment();
                }
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {
            }
        });
    }

    private void showTeamsFragment() {
        teamsMeetingTab.requestLayout();
        ViewCompat.setBackground(teamsMeetingTab, setImageButtonStateNew(context));
        lauchFragment(TeamsMeetingFragment.class.getName());
    }

    private void showGroupFragment() {
        groupMeetingTab.requestLayout();
        ViewCompat.setBackground(groupMeetingTab, setImageButtonStateNew(context));
        lauchFragment(GroupMeetingFragment.class.getName());
    }

    private void lauchFragment(final String fragmentClassName) {
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                fragmentClassName
        );

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.calling_fragment_container_view, fragment);
        transaction.commit();
    }

    private StateListDrawable setImageButtonStateNew(final Context mContext) {
        final StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_selected},
                ContextCompat.getDrawable(mContext, R.drawable.tab_background_normal_blue));
        states.addState(new int[] {-android.R.attr.state_selected},
                ContextCompat.getDrawable(mContext, R.drawable.tab_background_normal));

        return states;
    }
}
