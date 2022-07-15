package com.azure.samples.communication.ui.calling;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class JoinCallActivity extends AppCompatActivity {

    private TabLayout meetingTabLayout;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_call);

        if(savedInstanceState == null) {
            lauchFragment(GroupMeetingFragment.class.getName());
        }

        final ActionBar ab = getSupportActionBar();
        // Disable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Join");
        }
        initializeUI();

    }

    private void initializeUI() {

        meetingTabLayout = findViewById(R.id.meeting_tab_layout);

        meetingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Log.d("Mohtasim", "Tab name - " + tab.getText());
                if(tab.getText().equals("Group Meeting")) {
                    showGroupFragment();
                } else {
                    showTeamsFragment();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("Mohtasim", "Tab name - " + tab.getText());
            }
        });
    }

    private void showTeamsFragment() {
        Log.d("Mohtasim", "Teems Meeting is clicked");
        lauchFragment(TeamsMeetingFragment.class.getName());
    }

    private void showGroupFragment() {
        Log.d("Mohtasim", "Group Meeting is clicked " + GroupMeetingFragment.class.getName());
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
}