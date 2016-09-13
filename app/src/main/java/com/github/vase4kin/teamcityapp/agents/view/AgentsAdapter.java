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

package com.github.vase4kin.teamcityapp.agents.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Agents adapter
 */
public class AgentsAdapter extends RecyclerView.Adapter<AgentsAdapter.AgentViewHolder> {

    private static final String ICON = "{md-directions-railway}";

    private AgentDataModel mDataModel;

    public AgentsAdapter(AgentDataModel mDataModel) {
        this.mDataModel = mDataModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate(R.layout.item_with_title_list_not_clickable, viewGroup, false);
        return new AgentViewHolder(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(AgentViewHolder holder, int position) {
        String name = mDataModel.getName(position);
        holder.mTextView.setText(name);
        holder.mIcon.setText(ICON);
    }

    /**
     * Agent view holder
     */
    public static class AgentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemTitle)
        TextView mTextView;
        @BindView(R.id.itemIcon)
        IconTextView mIcon;

        public AgentViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
