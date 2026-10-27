/*
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

package teamcityapp.features.build_details.impl.data

import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.BuildLaunchData

class TeamCityBuildDetailsMetadataRepository(private val account: AccountScope, private val preferences: AccountPreferenceState, private val changes: teamcityapp.features.changes.api.ChangesRepository, private val tests: teamcityapp.features.tests.api.TestsRepository) : BuildDetailsMetadataRepository {
    override suspend fun count(build: BuildLaunchData, tab: BuildDetailsSection): Int = when (tab) {
        BuildDetailsSection.Changes -> changes.count(checkNotNull(build.changes?.href))
        BuildDetailsSection.Tests -> tests.summary(checkNotNull(build.tests?.href)).total
        else -> 0
    }
    override suspend fun pendingPrompts() = account.run { BuildDetailsPrompt.entries.filterNot { "details.${it.name}" in preferences.state.value.shownPrompts }.toSet() }
    override suspend fun acknowledgePrompt(prompt: BuildDetailsPrompt) {
        preferences.update { it.copy(shownPrompts = it.shownPrompts + "details.${prompt.name}") }
    }
}
