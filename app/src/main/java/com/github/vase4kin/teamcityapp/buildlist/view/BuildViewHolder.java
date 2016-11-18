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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Changes single item view holder
 */
public class BuildViewHolder extends BaseViewHolder<BuildListDataModel> {
    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.itemSubTitle)
    TextView mBranchName;
    @BindView(R.id.itemTitle)
    TextView mStatusText;
    @BindView(R.id.itemIcon)
    TextView mIcon;
    @BindView(R.id.buildNumber)
    TextView mBuildNumber;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public BuildViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_build_list_with_sub_title, parent, false));
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bind(BuildListDataModel dataModel, int position) {
        mIcon.setText(dataModel.getBuildStatusIcon(position));
        mStatusText.setText(dataModel.getStatusText(position));
        String buildNumber = dataModel.getBuildNumber(position);
        if (TextUtils.isEmpty(buildNumber)) {
            mBuildNumber.setVisibility(View.GONE);
        } else {
            mBuildNumber.setText(dataModel.getBuildNumber(position));
        }
        if (!dataModel.hasBranch(position)) {
            mBranchName.setVisibility(View.GONE);
            return;
        }
        mBranchName.setText(dataModel.getBranchName(position));
    }
}
