package com.azure.samples.communication.ui.calling.views.components;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.microsoft.fluentui.snackbar.Snackbar;
import com.azure.samples.communication.ui.calling.R;

public class ErrorInfoBar {

    private Snackbar snackbar;

    public void displayErrorInfoBar(View rootView, String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return;
        }
        snackbar = Snackbar.Companion.make(rootView, "", Snackbar.LENGTH_LONG, Snackbar.Style.REGULAR);
        snackbar.getView().setBackground(ContextCompat.getDrawable(rootView.getContext(), R.drawable.snackbar_background));
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbar.getView().getLayoutParams();
        DisplayMetrics metrics = rootView.getResources().getDisplayMetrics();

        params.setMargins(params.leftMargin + getDp(16, metrics),
                params.topMargin,
                params.rightMargin + getDp(16, metrics),
                params.bottomMargin + getDp(84, metrics));

        snackbar.getView().setLayoutParams(params);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);
        snackbar.setText((CharSequence)errorMessage);
        snackbar.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.snackbar_text_color));
        snackbar.show();
    }

    public void dismissErrorInfoBar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    private int getDp(int margin, DisplayMetrics metrics) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                margin,
                metrics
        );
    }
}
