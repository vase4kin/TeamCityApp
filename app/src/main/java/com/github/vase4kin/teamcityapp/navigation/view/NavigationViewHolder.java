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

package com.github.vase4kin.teamcityapp.navigation.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Changes single item view holder
 */
public class NavigationViewHolder extends BaseViewHolder<NavigationDataModel> {

    private static final String PROJECT = "{md-filter-none}";
    private static final String BUILD_TYPE = "{md-crop-din}";

    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.itemTitle)
    TextView mTextView;
    @BindView(R.id.itemSubTitle)
    TextView mDescription;
    @BindView(R.id.itemIcon)
    TextView mIcon;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public NavigationViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_with_title_and_sub_title_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(NavigationDataModel dataModel, int position) {
        mTextView.setText(dataModel.getName(position));
        if (dataModel.hasDescription(position)) {
            mDescription.setText(dataModel.getDescription(position));
            mDescription.setVisibility(View.VISIBLE);
        } else {
            mDescription.setVisibility(View.GONE);
        }
        if (dataModel.isProject(position)) {
            mIcon.setText(PROJECT);
        } else {
            mIcon.setText(BUILD_TYPE);
        }
    }
}
