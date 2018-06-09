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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Rate the app single item view holder
 */
public class RateTheAppViewHolder extends BaseViewHolder<NavigationDataModel> {

    private static final String ICON_RATE_THE_APP = "{md-shop}";

    @BindView(R.id.button_later)
    Button buttonLater;

    @BindView(R.id.button_rate)
    Button buttonRate;

    @BindView(R.id.itemIcon)
    TextView mIcon;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public RateTheAppViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_the_app, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(NavigationDataModel dataModel, int position) {
        mIcon.setText(ICON_RATE_THE_APP);
    }

    public void setListeners(View.OnClickListener later, View.OnClickListener rate) {
        buttonLater.setOnClickListener(later);
        buttonRate.setOnClickListener(rate);
    }
}
