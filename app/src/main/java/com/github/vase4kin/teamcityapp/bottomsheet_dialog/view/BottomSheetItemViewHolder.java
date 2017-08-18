/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Bottom sheet menu item view holder
 */
class BottomSheetItemViewHolder extends BaseViewHolder<BottomSheetDataModel> {

    @BindView(R.id.bs_title)
    TextView title;
    @BindView(R.id.bs_image)
    ImageView icon;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public BottomSheetItemViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_sheet, parent, false));
        ButterKnife.bind(this, itemView);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(BottomSheetDataModel dataModel, int position) {
        final String menuTitle = dataModel.getTitle(position);
        Drawable menuIcon = dataModel.getIcon(position);
        title.setText(menuTitle);
        icon.setImageDrawable(menuIcon);
    }
}
