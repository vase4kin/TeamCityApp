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

package com.github.vase4kin.teamcityapp.agents.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Agent view holder
 */
public class AgentViewHolder extends BaseViewHolder<AgentDataModel> {

    private static final String ICON = "{md-directions-railway}";

    @BindView(R.id.itemTitle)
    TextView mTextView;
    @BindView(R.id.itemIcon)
    IconTextView mIcon;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public AgentViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_with_title_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(AgentDataModel dataModel, int position) {
        String name = dataModel.getName(position);
        mTextView.setText(name);
        mIcon.setText(ICON);
    }
}
