/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.navigation.view

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.navigation.api.RateTheApp
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel

/**
 * Navigation items adapter
 */
class NavigationAdapter(
    viewHolderFactories: Map<Int, ViewHolderFactory<NavigationDataModel>>
) : BaseAdapter<NavigationDataModel>(viewHolderFactories) {

    private var onClickListener: OnNavigationItemClickListener? = null

    /**
     * Set [OnNavigationItemClickListener]
     *
     * @param mOnClickListener - listener to set
     */
    fun setOnClickListener(mOnClickListener: OnNavigationItemClickListener) {
        this.onClickListener = mOnClickListener
    }

    override fun onBindViewHolder(holder: BaseViewHolder<NavigationDataModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        // Find the way how to make it through DI
        if (holder is NavigationViewHolder) {
            holder.container.setOnClickListener { onClickListener?.onClick(dataModel.getNavigationItem(position)) }
        }
        if (holder is RateTheAppViewHolder) {
            holder.setListeners(
                { onClickListener?.onRateCancelButtonClick() },
                { onClickListener?.onRateNowButtonClick() })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataModel.isRateTheApp(position)) {
            NavigationView.TYPE_RATE_THE_APP
        } else {
            super.getItemViewType(position)
        }
    }

    fun removeRateTheApp() {
        dataModel.removeItemByIndex(RateTheApp.POSITION)
        notifyDataSetChanged()
    }
}
