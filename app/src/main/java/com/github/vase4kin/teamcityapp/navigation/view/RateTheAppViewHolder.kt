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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel

private const val ICON_RATE_THE_APP = "{md-shop}"

/**
 * Rate the app single item view holder
 */
class RateTheAppViewHolder(parent: ViewGroup) : BaseViewHolder<NavigationDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_rate_the_app,
        parent,
        false
    )
) {

    @BindView(R.id.button_cancel)
    lateinit var buttonCancel: Button
    @BindView(R.id.button_rate)
    lateinit var buttonRate: Button
    @BindView(R.id.itemIcon)
    lateinit var icon: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: NavigationDataModel, position: Int) {
        icon.text = ICON_RATE_THE_APP
    }

    fun setListeners(cancel: View.OnClickListener, rate: View.OnClickListener) {
        buttonCancel.setOnClickListener(cancel)
        buttonRate.setOnClickListener(rate)
    }
}
