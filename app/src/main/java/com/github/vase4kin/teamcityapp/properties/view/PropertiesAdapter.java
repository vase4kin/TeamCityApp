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

package com.github.vase4kin.teamcityapp.properties.view;

import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel;

import java.util.Map;

/**
 * Properties adapter
 */
public class PropertiesAdapter extends BaseAdapter<PropertiesDataModel> {

    private PropertiesView.Listener listener;

    public PropertiesAdapter(Map<Integer, ViewHolderFactory<PropertiesDataModel>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    void setOnCopyActionClickListener(PropertiesView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<PropertiesDataModel> holder, int position) {
        super.onBindViewHolder(holder, position);
        final String title = dataModel.getName(position);
        final String value = dataModel.getValue(position);
        ((PropertyViewHolder) holder).mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCardClick(title, value);
            }
        });
        ((PropertyViewHolder) holder).mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onCardClick(title, value);
                return true;
            }
        });
    }

}
