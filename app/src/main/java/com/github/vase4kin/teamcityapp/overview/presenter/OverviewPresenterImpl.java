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

package com.github.vase4kin.teamcityapp.overview.presenter;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModelImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment;
import com.github.vase4kin.teamcityapp.overview.view.OverviewView;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter to handle logic of {@link OverviewFragment}
 */
public class OverviewPresenterImpl extends BaseListPresenterImpl<
        BaseDataModel,
        BuildElement,
        OverviewView,
        OverViewInteractor,
        OverviewTracker,
        BaseValueExtractor> implements OverviewPresenter, OverviewView.OverviewViewListener, OverViewInteractor.OnOverviewEventsListener {

    @Inject
    OverviewPresenterImpl(@NonNull OverviewView view,
                          @NonNull OverViewInteractor dataManager,
                          @NonNull OverviewTracker tracker,
                          @NonNull BaseValueExtractor valueExtractor) {
        super(view, dataManager, tracker, valueExtractor);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mView.setOverViewListener(this);
        mDataManager.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<BuildElement>> loadingListener) {
        mDataManager.load(mValueExtractor.getBuild().getHref(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BaseDataModel createModel(List<BuildElement> data) {
        return new OverviewDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Build build = mValueExtractor.getBuild();
        if (build.isRunning()) {
            mView.createStopBuildOptionsMenu(menu, inflater);
        } else if (build.isQueued()) {
            mView.createRemoveBuildFromQueueOptionsMenu(menu, inflater);
        } else {
            mView.createDefaultOptionsMenu(menu, inflater);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mView.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelBuildContextMenuClick() {
        mDataManager.postStopBuildEvent();
        mTracker.trackUserClickedCancelBuildOption();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShareButtonClick() {
        mDataManager.postShareBuildInfoEvent();
        mTracker.trackUserSharedBuild();
    }

    @Override
    public void onRestartBuildButtonClick() {
        mDataManager.postRestartBuildEvent();
        // mTracker.trackUserClickedRestartBuildOption();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        mDataManager.subscribeToEventBusEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        mDataManager.unsubsribeFromEventBusEvents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDataRefreshEvent() {
        mView.showRefreshAnimation();
        onSwipeToRefresh();
        // TODO: Disable cancel build menu when data is updated and build has finished status
    }
}
