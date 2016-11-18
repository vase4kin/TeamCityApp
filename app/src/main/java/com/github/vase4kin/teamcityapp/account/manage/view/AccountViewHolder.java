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

package com.github.vase4kin.teamcityapp.account.manage.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.SelectableHolder;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountViewHolder extends BaseViewHolder<AccountDataModel> implements SelectableHolder {

    private static final String DEFAULT_ACCOUNT_ICON = "{md-account-circle}";
    private static final String SELECTED_ACCOUNT_ICON = "{md-check-circle}";

    @BindView(R.id.hightlited_zone)
    FrameLayout mHighlightedZone;
    @BindView(R.id.itemSubTitle)
    TextView mTeamCityUrlTextView;
    @BindView(R.id.itemTitle)
    TextView mUserNameTextView;
    @BindView(R.id.itemIcon)
    IconTextView mAccountIcon;
    @BindView(R.id.container)
    FrameLayout mContainer;

    private boolean isActive;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public AccountViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_account_list, parent, false));
        ButterKnife.bind(this, itemView);
        mHighlightedZone.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void bind(AccountDataModel dataModel, int position) {
        mTeamCityUrlTextView.setText(dataModel.getTeamcityUrl(position));
        mUserNameTextView.setText(dataModel.getUserName(position));
        mAccountIcon.setText(DEFAULT_ACCOUNT_ICON);
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void setSelectable(boolean b) {
    }

    @Override
    public boolean isActivated() {
        return isActive;
    }

    @Override
    public void setActivated(boolean b) {
        if (b) {
            mAccountIcon.setText(SELECTED_ACCOUNT_ICON);
            mHighlightedZone.setVisibility(View.VISIBLE);
        } else {
            mAccountIcon.setText(DEFAULT_ACCOUNT_ICON);
            mHighlightedZone.setVisibility(View.GONE);
        }
        isActive = b;
    }
}
