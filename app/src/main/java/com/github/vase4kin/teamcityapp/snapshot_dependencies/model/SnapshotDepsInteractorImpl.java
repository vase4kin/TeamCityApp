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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.model;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;

import java.util.List;

/**
 * Snapshot dependencies build list interactor
 */
public class SnapshotDepsInteractorImpl extends BuildListDataManagerImpl implements BuildListDataManager {

    public SnapshotDepsInteractorImpl(Repository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String id, @NonNull OnLoadingListener<List<BuildDetails>> loadingListener, boolean update) {
        loadNotSortedBuilds(mRepository.listSnapshotBuilds(id, update), loadingListener);
    }
}
