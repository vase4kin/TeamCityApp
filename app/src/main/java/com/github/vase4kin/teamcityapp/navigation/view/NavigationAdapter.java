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

import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.navigation.api.RateTheApp;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;

import java.util.Map;

/**
 * Navigation items adapter
 */
public class NavigationAdapter extends BaseAdapter<NavigationDataModel> {

    private OnNavigationItemClickListener mOnClickListener;

    /**
     * Constructor
     *
     * @param viewHolderFactories - view holder factories from DI
     */
    public NavigationAdapter(Map<Integer, ViewHolderFactory<NavigationDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    /**
     * Set {@link OnNavigationItemClickListener}
     *
     * @param mOnClickListener - listener to set
     */
    public void setOnClickListener(OnNavigationItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<NavigationDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        // Find the way how to make it through DI
        final int adapterPosition = position;
        if (holder instanceof NavigationViewHolder) {
            ((NavigationViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onClick(mDataModel.getNavigationItem(adapterPosition));
                }
            });
        }
        if (holder instanceof RateTheAppViewHolder) {
            RateTheAppViewHolder rateTheAppViewHolder = (RateTheAppViewHolder) holder;
            rateTheAppViewHolder.setListeners(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onRateCancelButtonClick();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onRateNowButtonClick();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataModel.isRateTheApp(position)) {
            return NavigationView.TYPE_RATE_THE_APP;
        } else {
            return super.getItemViewType(position);
        }
    }

    public void removeRateTheApp() {
        mDataModel.removeItemByIndex(RateTheApp.POSITION);
        notifyItemRemoved(RateTheApp.POSITION);
    }
}
