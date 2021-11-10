// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.helpers;

import android.graphics.drawable.Drawable;

public class BottomCellItem {
    private Drawable avatar;
    private String title;
    private Drawable accessoryImage;
    private Boolean enabled;
    private Runnable onClickAction;

    public BottomCellItem() { }

    public BottomCellItem(final Drawable avatar,
                          final String title, final Drawable accessoryImage,
                          final Boolean enabled, final Runnable onClickAction) {
        this.avatar = avatar;
        this.title = title;
        this.accessoryImage = accessoryImage;
        this.enabled = enabled;
        this.onClickAction = onClickAction;
    }

    public void setAvatar(final Drawable avatar) {
        this.avatar = avatar;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setAccessoryImage(final Drawable accessoryImage) {
        this.accessoryImage = accessoryImage;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnClickAction(final Runnable onClickAction) {
        this.onClickAction = onClickAction;
    }

    public Drawable getAvatar() {
        return avatar;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getAccessoryImage() {
        return accessoryImage;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Runnable getOnClickAction() {
        return onClickAction;
    }

}
