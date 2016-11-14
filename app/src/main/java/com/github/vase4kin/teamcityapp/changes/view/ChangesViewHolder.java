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

package com.github.vase4kin.teamcityapp.changes.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Changes single item view holder
 */
public class ChangesViewHolder extends BaseViewHolder<ChangesDataModel> {
    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.itemSubTitle)
    TextView mItemSubTitle;
    @BindView(R.id.itemTitle)
    TextView mItemTitle;
    @BindView(R.id.itemIcon)
    TextView mIcon;
    @BindView(R.id.userName)
    TextView mUserName;
    @BindView(R.id.date)
    TextView mDate;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public ChangesViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changes_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(final ChangesDataModel dataModel, final int position) {
        mIcon.setText(IconUtils.getCountIcon(dataModel.getFilesCount(position)));
        mItemTitle.setText(dataModel.getComment(position));
        mUserName.setText(dataModel.getUserName(position));
        mDate.setText(dataModel.getDate(position));
        mItemSubTitle.setText(dataModel.getVersion(position));
    }
}
