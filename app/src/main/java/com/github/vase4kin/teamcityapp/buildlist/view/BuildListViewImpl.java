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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.utils.DateUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.mugen.Mugen;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import tr.xip.errorview.ErrorView;

/**
 * Impl of {@link BuildListView}
 */
public class BuildListViewImpl extends BaseListViewImpl<BuildListDataModel, SimpleSectionedRecyclerViewAdapter<BuildListAdapter>> implements BuildListView {

    @BindView(R.id.floating_action_button)
    FloatingActionButton mFloatingActionButton;
    private List<SimpleSectionedRecyclerViewAdapter.Section> mSections;
    private BuildListDataModel mDataModel;

    protected OnBuildListPresenterListener mOnBuildListPresenterListener;

    public BuildListViewImpl(View mView,
                             Activity activity,
                             @StringRes int emptyMessage,
                             SimpleSectionedRecyclerViewAdapter<BuildListAdapter> adapter) {
        super(mView, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    public void setOnBuildListPresenterListener(OnBuildListPresenterListener onBuildListPresenterListener) {
        this.mOnBuildListPresenterListener = onBuildListPresenterListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull ErrorView.RetryListener retryListener,
                          @NonNull SwipeRefreshLayout.OnRefreshListener refreshListener) {
        super.initViews(retryListener, refreshListener);
        mFloatingActionButton.setImageDrawable(new IconDrawable(mActivity, MaterialIcons.md_directions_run).color(Color.WHITE));
        mFloatingActionButton.hide();
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnBuildListPresenterListener.onRunBuildFabClick();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(String title) {
        ActionBar actionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildFloatActionButton() {
        mFloatingActionButton.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRunBuildFloatActionButton() {
        mFloatingActionButton.hide();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.addLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.removeLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRetryLoadMoreSnackBar() {
        Snackbar snackBar = Snackbar.make(
                mRecyclerView,
                R.string.load_more_retry_snack_bar_text,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBuildListPresenterListener.onLoadMore();
                    }
                });
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(BuildListDataModel dataModel) {
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.addMoreBuilds(dataModel);
        reInitSections(mDataModel);
        setSections(mSections);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(BuildListDataModel dataModel) {
        this.mDataModel = dataModel;
        mSections = initSections(dataModel);
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnBuildListPresenterListener(mOnBuildListPresenterListener);
        initSectionAdapter();
        Mugen.with(mRecyclerView, mOnBuildListPresenterListener).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.build_recycler_view;
    }

    /**
     * Init sectionAdapter
     *
     * @param
     */
    private void initSectionAdapter() {
        setSections(mSections);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Set sections for mSectionAdapter
     *
     * @param sections
     */
    private void setSections(List<SimpleSectionedRecyclerViewAdapter.Section> sections) {
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mAdapter.setSections(sections.toArray(userStates));
    }

    /**
     * Return sections for build data
     *
     * @param dataModel
     * @return List<SimpleSectionedRecyclerViewAdapter.Section>
     */
    private List<SimpleSectionedRecyclerViewAdapter.Section> initSections(BuildListDataModel dataModel) {
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();

        if (dataModel.getItemCount() != 0) {
            for (int i = 0; i < dataModel.getItemCount(); i++) {
                String buildDate = DateUtils.initWithDate(dataModel.getStartDate(i)).formatStartDateToBuildListItemHeader();
                if (sections.size() != 0) {
                    SimpleSectionedRecyclerViewAdapter.Section prevSection = sections.get(sections.size() - 1);
                    if (!prevSection.getTitle().equals(buildDate)) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, buildDate));
                    }
                } else {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, buildDate));
                }
            }
        }
        return sections;
    }

    /**
     * ReInit sections if more builds are loaded
     */
    private void reInitSections(BuildListDataModel dataModel) {
        mSections.clear();
        mSections.addAll(initSections(dataModel));
    }
}
