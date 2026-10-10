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

package com.github.vase4kin.teamcityapp.buildlist.data

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import teamcityapp.features.build_history.api.BuildHistoryOnboardingRepository
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.onboarding.OnboardingManager

/** Uses the installed global preference keys; feature UI owns anchored prompt rendering. */
class AppBuildHistoryOnboardingRepository @Inject constructor(
    private val onboarding: OnboardingManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BuildHistoryOnboardingRepository {
    override suspend fun pendingPrompts(): List<BuildHistoryPrompt> = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        buildList {
            if (!onboarding.isRunBuildPromptShown) add(BuildHistoryPrompt.Run)
            if (!onboarding.isFilterBuildsPromptShown) add(BuildHistoryPrompt.Filter)
            if (!onboarding.isFavPromptShown) add(BuildHistoryPrompt.Favorite)
        }
    }
    override suspend fun markShown(prompt: BuildHistoryPrompt): Unit = withContext(ioDispatcher) {
        currentCoroutineContext().ensureActive()
        when (prompt) {
            BuildHistoryPrompt.Run -> onboarding.saveRunBuildPromptShown()
            BuildHistoryPrompt.Filter -> onboarding.saveFilterBuildsPromptShown()
            BuildHistoryPrompt.Favorite -> onboarding.saveFavPromptShown()
        }
    }
}
