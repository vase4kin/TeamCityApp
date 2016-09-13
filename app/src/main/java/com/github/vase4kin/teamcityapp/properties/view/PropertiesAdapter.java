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

package com.github.vase4kin.teamcityapp.properties.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Properties adapter
 */
public class PropertiesAdapter extends RecyclerView.Adapter<PropertiesAdapter.PropertyViewHolder> {

    private PropertiesDataModel mDataModel;
    private OnCopyActionClickListener mOnCopyActionClickListener;

    public PropertiesAdapter(PropertiesDataModel mDataModel, OnCopyActionClickListener onPropertyClickListener) {
        this.mDataModel = mDataModel;
        this.mOnCopyActionClickListener = onPropertyClickListener;
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
    public PropertyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate(R.layout.item_simple_element_list, viewGroup, false);
        return new PropertyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PropertyViewHolder holder, final int position) {
        holder.mHeader.setText(mDataModel.getName(position));
        holder.mTextView.setText(mDataModel.getValue(position));
        if (mDataModel.isEmpty(position)) {
            holder.mTextView.setTextColor(Color.LTGRAY);
        } else {
            int color = holder.mTextView.getContext().getResources().getColor(R.color.abc_primary_text_material_light);
            holder.mTextView.setTextColor(color);
        }
        holder.mIcon.setVisibility(View.GONE);
        OnCopyActionAdapterListenerImpl listener =
                new OnCopyActionAdapterListenerImpl(
                        mDataModel.getName(position),
                        mDataModel.getValue(position),
                        mOnCopyActionClickListener);
        holder.mContainer.setOnClickListener(listener);
        holder.mContainer.setOnLongClickListener(listener);
    }

    public static class PropertyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        FrameLayout mContainer;
        @BindView(R.id.itemTitle)
        TextView mTextView;
        @BindView(R.id.itemHeader)
        TextView mHeader;
        @BindView(R.id.itemIcon)
        IconTextView mIcon;

        public PropertyViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
