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

package com.github.vase4kin.teamcityapp.overview.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Overview single item view holder
 */
public class OverviewViewHolder extends BaseViewHolder<OverviewDataModel> {
    @BindView(R.id.container)
    FrameLayout mFrameLayout;
    @BindView(R.id.itemIcon)
    TextView icon;
    @BindView(R.id.itemTitle)
    TextView description;
    @BindView(R.id.itemHeader)
    TextView mHeader;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public OverviewViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_element_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(OverviewDataModel dataModel, int position) {
        icon.setText(dataModel.getIcon(position));
        mHeader.setText(dataModel.getHeaderName(position));
        description.setText(dataModel.getDescription(position));
    }
}
