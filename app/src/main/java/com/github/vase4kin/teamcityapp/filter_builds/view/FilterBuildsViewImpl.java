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

package com.github.vase4kin.teamcityapp.filter_builds.view;

import android.graphics.Color;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Impl of {@link FilterBuildsView}
 */
public class FilterBuildsViewImpl implements FilterBuildsView {

    @BindView(R.id.fab_filter)
    FloatingActionButton mFilterFab;

    @BindView(R.id.selected_filter)
    TextView mSelectedFilterStatus;

    @BindView(R.id.switcher_is_personal)
    Switch mPersonalSwitch;

    @BindView(R.id.switcher_is_pinned)
    Switch mPinnedSwitch;

    @BindView(R.id.divider_switcher_is_pinned)
    View mPinnedSwitcherDivider;

    @OnClick(R.id.chooser_filter)
    public void onFilterChooserClick() {
        mFilterChooser.show();
    }

    private FilterBuildsActivity mActivity;
    private Unbinder mUnbinder;

    private MaterialDialog mFilterChooser;

    private int mSelectedFilter = FILTER_NONE;

    public FilterBuildsViewImpl(FilterBuildsActivity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(final ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);

        Toolbar mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(mToolbar);

        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_filter_builds);
        }

        // For ui testing purpose
        mToolbar.setNavigationContentDescription(R.string.navigate_up);
        mToolbar.setNavigationIcon(new IconDrawable(mActivity, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        mToolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(listener));

        mFilterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFilterFabClick(mSelectedFilter, mPersonalSwitch.isChecked(), mPinnedSwitch.isChecked());
            }
        });

        mFilterChooser = new MaterialDialog.Builder(mActivity)
                .title(R.string.title_filter_chooser_dialog)
                .items(R.array.build_filters)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == FILTER_QUEUED) {
                            listener.onQueuedFilterSelected();
                        } else {
                            listener.onOtherFiltersSelected();
                        }
                        mSelectedFilterStatus.setText(text);
                        mSelectedFilter = position;
                    }
                })
                .build();
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
    public void hideSwitchForPinnedFilter() {
        mPinnedSwitch.setVisibility(View.GONE);
        mPinnedSwitcherDivider.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showSwitchForPinnedFilter() {
        mPinnedSwitch.setVisibility(View.VISIBLE);
        mPinnedSwitcherDivider.setVisibility(View.VISIBLE);
    }
}
