// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.samples.communication.calling.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azure.samples.communication.calling.R;
import com.azure.samples.communication.calling.helpers.BottomCellItem;

public class BottomCellViewHolder extends RecyclerView.ViewHolder {
    private ImageView avatar;
    private TextView title;
    private ImageView accessoryImage;
    private Runnable onClickAction;


    public BottomCellViewHolder(@NonNull final View itemView) {
        super(itemView);
        avatar = (ImageView) itemView.findViewById(R.id.icon_for_audio_device);
        title = (TextView) itemView.findViewById(R.id.text_for_audio_device);
        accessoryImage = (ImageView) itemView.findViewById(R.id.check_mark_for_audio_device);
        itemView.setOnClickListener(v -> onClickAction.run());
    }

    public void setCellData(final BottomCellItem bottomCellItem) {
        this.avatar.setImageDrawable(bottomCellItem.getAvatar());
        this.title.setText(bottomCellItem.getTitle());
        this.accessoryImage.setImageDrawable(bottomCellItem.getAccessoryImage());
        if (bottomCellItem.getEnabled()) {
            this.accessoryImage.setVisibility(View.VISIBLE);
        } else {
            this.accessoryImage.setVisibility(View.INVISIBLE);
        }
        this.onClickAction = bottomCellItem.getOnClickAction();
    }
}
