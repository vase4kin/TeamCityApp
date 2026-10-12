/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.features.snapshot_dependencies.impl.data

import com.github.vase4kin.teamcityapp.api.Repository
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.data.builds.BuildHydrator

class DefaultSnapshotDependenciesRepository @Inject constructor(
    private val repository: Provider<Repository>,
    private val hydrator: BuildHydrator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SnapshotDependenciesRepository {
    override suspend fun dependencies(buildId: String, forceRefresh: Boolean): List<BuildLaunchData> = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        val session = repository.get()
        // Repository preserves snapshotDependency:(to:(id:ID),includeInitial:true),defaultFilter:false.
        val page = session.listSnapshotBuilds(buildId, forceRefresh).await()
        currentCoroutineContext().ensureActive()
        if (page.count == 0) emptyList() else hydrator.hydrate(session, page.objects.orEmpty().toList())
    }
}
