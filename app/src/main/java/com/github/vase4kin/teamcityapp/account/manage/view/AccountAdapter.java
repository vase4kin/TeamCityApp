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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.MultiSelectorBindingHolder;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to handle data for {@link AccountListActivity}
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.UserAccountViewHolder> {

    private static final String DEFAULT_ACCOUNT_ICON = "{md-account-circle}";
    private static final String SELECTED_ACCOUNT_ICON = "{md-check-circle}";

    private AccountDataModel mDataModel;
    private MultiSelector mMultiSelector;
    private OnUserAccountClickListener mListener;

    public AccountAdapter(AccountDataModel mDataModel, MultiSelector mMultiSelector, OnUserAccountClickListener mListener) {
        this.mDataModel = mDataModel;
        this.mMultiSelector = mMultiSelector;
        this.mListener = mListener;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    @Override
    public UserAccountViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate(R.layout.item_user_account_list, viewGroup, false);
        return new UserAccountViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserAccountViewHolder holder, int position) {
        holder.mTeamCityUrlTextView.setText(mDataModel.getTeamcityUrl(position));
        holder.mUserNameTextView.setText(mDataModel.getUserName(position));
        holder.mAccountIcon.setText(DEFAULT_ACCOUNT_ICON);
        holder.setClickListeners(position);
    }

    public class UserAccountViewHolder extends MultiSelectorBindingHolder {
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

        public UserAccountViewHolder(View v) {
            super(v, mMultiSelector);
            ButterKnife.bind(this, v);
            mHighlightedZone.setBackgroundColor(Color.LTGRAY);
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

        void setClickListeners(final int position) {
            mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mListener.click(position);
                    return true;
                }
            });
            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.click(position);
                }
            });
        }
    }
}
