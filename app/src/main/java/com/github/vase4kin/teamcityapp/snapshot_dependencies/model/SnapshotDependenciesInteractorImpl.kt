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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.model

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage

/**
 * Snapshot dependencies build list interactor
 */
class SnapshotDependenciesInteractorImpl(
        repository: Repository,
        storage: SharedUserStorage
) : BuildListDataManagerImpl(repository, storage), SnapshotDependenciesInteractor {

    /**
     * {@inheritDoc}
     */
    override fun load(id: String, loadingListener: OnLoadingListener<List<BuildDetails>>, update: Boolean) {
        loadNotSortedBuilds(repository.listSnapshotBuilds(id, update), loadingListener)
    }
}

interface SnapshotDependenciesInteractor : BuildListDataManager
