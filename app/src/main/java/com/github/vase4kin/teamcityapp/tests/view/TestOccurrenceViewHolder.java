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

package com.github.vase4kin.teamcityapp.tests.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Test single item view holder
 */
public class TestOccurrenceViewHolder extends BaseViewHolder<TestsDataModel> {
    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.itemTitle)
    TextView mTextView;
    @BindView(R.id.itemIcon)
    IconTextView mIcon;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public TestOccurrenceViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_occurence_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(TestsDataModel dataModel, int position) {
        mTextView.setText(dataModel.getName(position));
        mIcon.setText(dataModel.getStatusIcon(position));
    }
}
