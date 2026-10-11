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

package com.github.vase4kin.teamcityapp.buildlist.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.onboarding.OnboardingManager

@OptIn(ExperimentalCoroutinesApi::class)
class AppBuildHistoryOnboardingRepositoryTest {
    @Test fun pendingPromptsKeepRunFilterFavoriteSequence() = runTest {
        val manager = mock<OnboardingManager>()
        assertEquals(listOf(BuildHistoryPrompt.Run, BuildHistoryPrompt.Filter, BuildHistoryPrompt.Favorite), AppBuildHistoryOnboardingRepository(manager, StandardTestDispatcher(testScheduler)).pendingPrompts())
        verify(manager).isRunBuildPromptShown
        verify(manager).isFilterBuildsPromptShown
        verify(manager).isFavPromptShown
        verifyNoMoreInteractions(manager)
    }

    @Test fun completedFlagsAreOmittedAndAddFavoriteHomePromptIsIndependent() = runTest {
        val manager = mock<OnboardingManager> {
            on { isRunBuildPromptShown } doReturn true
            on { isFavPromptShown } doReturn true
        }
        assertEquals(listOf(BuildHistoryPrompt.Filter), AppBuildHistoryOnboardingRepository(manager, StandardTestDispatcher(testScheduler)).pendingPrompts())
        verify(manager, never()).isAddFavPromptShown
    }

    @Test fun markShownWritesOnlyTheCorrespondingExistingPreference() = runTest {
        val manager = mock<OnboardingManager>()
        val adapter = AppBuildHistoryOnboardingRepository(manager, StandardTestDispatcher(testScheduler))
        adapter.markShown(BuildHistoryPrompt.Run)
        adapter.markShown(BuildHistoryPrompt.Filter)
        adapter.markShown(BuildHistoryPrompt.Favorite)
        verify(manager).saveRunBuildPromptShown()
        verify(manager).saveFilterBuildsPromptShown()
        verify(manager).saveFavPromptShown()
        verifyNoMoreInteractions(manager)
    }

    @Test fun failedPersistencePropagatesForFeatureRetryWithoutMarkingOtherPrompts() = runTest {
        val manager = mock<OnboardingManager>()
        val failure = IllegalStateException("unavailable")
        doThrow(failure).whenever(manager).saveFilterBuildsPromptShown()
        try {
            AppBuildHistoryOnboardingRepository(manager, StandardTestDispatcher(testScheduler)).markShown(BuildHistoryPrompt.Filter)
            fail("Expected persistence failure")
        } catch (actual: IllegalStateException) {
            assertEquals(failure::class, actual::class)
            assertEquals(failure.message, actual.message)
        }
        verify(manager).saveFilterBuildsPromptShown()
        verifyNoMoreInteractions(manager)
    }

    @Test fun cancelledPromptPersistenceDoesNotChangeStorage() = runTest {
        val manager = mock<OnboardingManager>()
        val adapter = AppBuildHistoryOnboardingRepository(manager, StandardTestDispatcher(testScheduler))
        val request = async { adapter.markShown(BuildHistoryPrompt.Run) }
        request.cancelAndJoin()
        verifyNoInteractions(manager)
    }
}
