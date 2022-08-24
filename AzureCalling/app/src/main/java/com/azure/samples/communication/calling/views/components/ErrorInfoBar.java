// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.views.components;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import com.azure.samples.communication.calling.R;
import com.microsoft.fluentui.snackbar.Snackbar;

public class ErrorInfoBar {

    private Snackbar snackbar;

    public void displayErrorInfoBar(final View rootView, final String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return;
        }
        snackbar = Snackbar.Companion.make(rootView, "", Snackbar.LENGTH_LONG, Snackbar.Style.REGULAR);
        snackbar.getView()
                .setBackground(ContextCompat.getDrawable(rootView.getContext(), R.drawable.snackbar_background));
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbar.getView().getLayoutParams();
        final DisplayMetrics metrics = rootView.getResources().getDisplayMetrics();

        params.setMargins(params.leftMargin + getDp(16, metrics),
                params.topMargin,
                params.rightMargin + getDp(16, metrics),
                params.bottomMargin + getDp(84, metrics));

        snackbar.getView().setLayoutParams(params);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);
        snackbar.setText((CharSequence) errorMessage);
        snackbar.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.snackbar_text_color));
        snackbar.show();
    }

    public void dismissErrorInfoBar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    private int getDp(final int margin, final DisplayMetrics metrics) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                margin,
                metrics
        );
    }
}
