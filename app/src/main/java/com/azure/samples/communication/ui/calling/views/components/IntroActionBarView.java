package com.azure.samples.communication.ui.calling.views.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.azure.samples.communication.ui.calling.R;
import com.microsoft.fluentui.persona.AvatarView;

public class IntroActionBarView extends ConstraintLayout {
    TextView usernameTextView;
    AvatarView avatarView;


    public IntroActionBarView(@NonNull Context context) {
        super(context);
    }

    public IntroActionBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IntroActionBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IntroActionBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //ToDo: Wire up to actual API values
        avatarView = findViewById(R.id.avatar_view);
        usernameTextView = findViewById(R.id.username_textview);
        usernameTextView.setText("Mona Kane");
        Button signoutButton = findViewById(R.id.signout_button);
        signoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("IntroActionBarView","Clicked Signout button");
            }
        });
    }
}
