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

package com.github.vase4kin.teamcityapp.runningbuilds.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter to handle {@link com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListActivity}
 */
public class RunningBuildsListPresenterImpl extends BuildListPresenterImpl<RunningBuildListView, RunningBuildsDataManager> {

    @Inject
    RunningBuildsListPresenterImpl(@NonNull RunningBuildListView view,
                                   @NonNull RunningBuildsDataManager dataManager,
                                   @NonNull ViewTracker tracker,
                                   @NonNull BuildListRouter router,
                                   @NonNull BaseValueExtractor valueExtractor) {
        super(view, dataManager, tracker, valueExtractor, router);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<Build>> loadingListener) {
        mDataManager.load(loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSuccessCallBack(List<Build> data) {
        super.onSuccessCallBack(data);
        mView.updateTitle(data.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFailCallBack(String errorMessage) {
        super.onFailCallBack(errorMessage);
        mView.updateTitle(0);
    }
}
