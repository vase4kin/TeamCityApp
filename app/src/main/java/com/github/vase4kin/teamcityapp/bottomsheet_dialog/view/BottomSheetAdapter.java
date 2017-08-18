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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;

/**
 * Adapter to manage bottom sheet items
 */
public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.BottomSheetItemViewHolder> {

    private final BottomSheetDataModel model;
    private final BottomSheetView.OnBottomSheetClickListener listener;

    public BottomSheetAdapter(BottomSheetDataModel model, BottomSheetView.OnBottomSheetClickListener listener) {
        this.model = model;
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BottomSheetItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_sheet, parent, false);
        return new BottomSheetItemViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(BottomSheetItemViewHolder holder, int position) {
        String title = model.getTitle(position);
        holder.description.setText(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return model.getItemCount();
    }

    /**
     * Bottom sheet menu item view holder
     */
    static class BottomSheetItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView description;

        BottomSheetItemViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.bs_title);
        }
    }
}
