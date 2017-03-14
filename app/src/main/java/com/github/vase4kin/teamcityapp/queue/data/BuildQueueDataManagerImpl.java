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

package com.github.vase4kin.teamcityapp.queue.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager;

import java.util.List;

/**
 * Data manager to handle build queue server operations
 */
public class BuildQueueDataManagerImpl extends BuildListDataManagerImpl implements RunningBuildsDataManager {

    public BuildQueueDataManagerImpl(Repository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull OnLoadingListener<List<BuildDetails>> loadingListener) {
        loadBuilds(mRepository.listQueueBuilds(null), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCount(@NonNull OnLoadingListener<Integer> loadingListener) {
        loadCount(mRepository.listQueueBuilds("count"), loadingListener);
    }
}
