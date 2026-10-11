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

package com.github.vase4kin.teamcityapp.build_details.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.builds.data.homeBuildAccountKey
import com.github.vase4kin.teamcityapp.builds.data.requireCurrentHomeBuildAccount
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.onboarding.OnboardingManager

class AppBuildDetailsMetadataRepository @Inject constructor(
    private val repositories: Provider<Repository>,
    private val storage: SharedUserStorage,
    private val onboarding: OnboardingManager,
    @IoDispatcher private val io: CoroutineDispatcher
) : BuildDetailsMetadataRepository {
    override suspend fun count(build: BuildLaunchData, tab: BuildDetailsTab): Int = withContext(io) {
        val account = storage.activeUser.homeBuildAccountKey()
        requireCurrentHomeBuildAccount(storage, account)
        val session = repositories.get()
        requireCurrentHomeBuildAccount(storage, account)
        val count = try {
            when (tab) {
                BuildDetailsTab.Changes -> session.listChanges("${checkNotNull(build.changes?.href)},count:${Int.MAX_VALUE}&fields=count", true).await().count
                BuildDetailsTab.Tests -> session.listTestOccurrences("${checkNotNull(build.tests?.href)},count:${Int.MAX_VALUE}&fields=count", true).await().count
                else -> error("Only Changes and Tests expose counts")
            }
        } catch (error: Exception) {
            requireCurrentHomeBuildAccount(storage, account)
            throw error
        }
        requireCurrentHomeBuildAccount(storage, account)
        count
    }
    override suspend fun pendingPrompts(): Set<BuildDetailsPrompt> = withContext(io) {
        buildSet {
            if (!onboarding.isStopBuildPromptShown) add(BuildDetailsPrompt.Stop)
            if (!onboarding.isRemoveBuildFromQueuePromptShown) add(BuildDetailsPrompt.Remove)
            if (!onboarding.isRestartBuildPromptShown) add(BuildDetailsPrompt.Restart)
        }
    }
    override suspend fun acknowledgePrompt(prompt: BuildDetailsPrompt) = withContext(io) {
        when (prompt) {
            BuildDetailsPrompt.Stop -> onboarding.saveStopBuildPromptShown()
            BuildDetailsPrompt.Remove -> onboarding.saveRemoveBuildFromQueuePromptShown()
            BuildDetailsPrompt.Restart -> onboarding.saveRestartBuildPromptShown()
        }
    }
}
