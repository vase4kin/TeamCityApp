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

package com.github.vase4kin.teamcityapp.navigation.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.github.vase4kin.teamcityapp.navigation.api.Project
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.features.navigation.api.NavigationRepository
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.coroutines.IoDispatcher

/** Preserves the existing node cache and section order behind immutable feature models. */
class AppNavigationRepository @Inject constructor(
    private val repository: Provider<Repository>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NavigationRepository {
    override suspend fun entries(projectId: String, forceRefresh: Boolean): List<NavigationEntry> = withContext(ioDispatcher) {
        repository.get().listBuildTypes(projectId, forceRefresh).await().objects.map { item ->
            when (item) {
                is Project -> NavigationEntry.Project(ProjectReference(item.id.orEmpty(), item.name.orEmpty()), item.description)
                is BuildType -> NavigationEntry.Configuration(BuildConfigurationSummary(item.id.orEmpty(), item.name.orEmpty(), item.description, ProjectReference(item.projectId.orEmpty(), item.projectName.orEmpty())))
                else -> error("Unexpected navigation item: ${item.javaClass.name}")
            }
        }
    }
}
