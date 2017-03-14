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

package com.github.vase4kin.teamcityapp.runningbuilds.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter;
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl;
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

import java.util.List;

/**
 * Impl of {@link RunningBuildsDataManager}
 */
public class RunningBuildsDataManagerImpl extends BuildListDataManagerImpl implements RunningBuildsDataManager {

    /**
     * Filter to show only running builds
     */
    private BuildListFilter mFilter;

    public RunningBuildsDataManagerImpl(Repository repository) {
        super(repository);
        // Creating running filter
        mFilter = new BuildListFilterImpl();
        mFilter.setFilter(FilterBuildsView.FILTER_RUNNING);
    }

    /**
     * {@inheritDoc}
     *
     * TODO: WTF RUNNING BUILDS?
     */
    @Override
    public void load(@NonNull OnLoadingListener<List<BuildDetails>> loadingListener, boolean update) {
        loadBuilds(mRepository.listRunningBuilds(mFilter.toLocator(), null, update), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCount(@NonNull OnLoadingListener<Integer> loadingListener) {
        loadCount(mRepository.listRunningBuilds(mFilter.toLocator(), "count", false), loadingListener);
    }
}
