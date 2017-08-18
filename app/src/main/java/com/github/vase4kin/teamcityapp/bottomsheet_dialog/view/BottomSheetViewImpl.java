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

import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Impl of {@link BottomSheetView}
 */
public class BottomSheetViewImpl implements BottomSheetView {

    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.bs_main_title)
    TextView mainTitle;

    private Unbinder mUnbinder;
    private final View view;
    private final BottomSheetDialogFragment fragment;
    private final BottomSheetAdapter adapter;

    public BottomSheetViewImpl(View view, BottomSheetDialogFragment fragment, BottomSheetAdapter adapter) {
        this.view = view;
        this.fragment = fragment;
        this.adapter = adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(OnBottomSheetClickListener listener, BottomSheetDataModel dataModel, String title) {
        mUnbinder = ButterKnife.bind(this, view);
        mainTitle.setText(title);
        adapter.setDataModel(dataModel);
        adapter.setListener(listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        mUnbinder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        fragment.dismiss();
    }
}
