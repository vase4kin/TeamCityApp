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

package com.github.vase4kin.teamcityapp.properties.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Properties single item view holder
 */
public class PropertyViewHolder extends BaseViewHolder<PropertiesDataModel> {
    @BindColor(R.color.abc_primary_text_material_light)
    int mEmptyColor;
    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.itemTitle)
    TextView mTextView;
    @BindView(R.id.itemHeader)
    TextView mHeader;
    @BindView(R.id.itemIcon)
    IconTextView mIcon;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public PropertyViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_element_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(PropertiesDataModel dataModel, int position) {
        mHeader.setText(dataModel.getName(position));
        mTextView.setText(dataModel.getValue(position));
        if (dataModel.isEmpty(position)) {
            mTextView.setTextColor(Color.LTGRAY);
        } else {
            mTextView.setTextColor(mEmptyColor);
        }
        mIcon.setVisibility(View.GONE);
    }
}
