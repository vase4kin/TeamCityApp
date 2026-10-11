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

package com.github.vase4kin.teamcityapp.overview.router

import android.app.Activity
import android.app.Application
import android.os.Handler
import com.github.vase4kin.teamcityapp.overview.data.loadedOverview
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.onboarding.OnboardingManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ToolbarBuildOverviewPromptsTest {
    private val activity = mock<Activity>()
    private val preferences = mock<OnboardingManager>()
    private val handler = mock<Handler>()
    private val prompts = ToolbarBuildOverviewPrompts(activity, preferences, handler)

    @Test fun loadedRunningStatusChecksSharedStopPreferenceAndSchedulesNativeOverflowPrompt() {
        prompts.resumed(loadedOverview)
        verify(preferences).isStopBuildPromptShown
        verify(preferences, never()).isRestartBuildPromptShown
        verify(handler).postDelayed(any(), eq(500L))
    }

    @Test fun loadedFinishedStatusChecksTheSharedRestartPreference() {
        prompts.resumed(loadedOverview.copy(state = "finished"))
        verify(preferences).isRestartBuildPromptShown
        verify(preferences, never()).isStopBuildPromptShown
        verify(handler).postDelayed(any(), eq(500L))
    }

    @Test fun loadedQueuedStatusChecksTheSharedRemovalPreference() {
        prompts.resumed(loadedOverview.copy(state = "queued"))
        verify(preferences).isRemoveBuildFromQueuePromptShown
        verify(handler).postDelayed(any(), eq(500L))
    }

    @Test fun alreadyHandledPromptDoesNotCreateUiWork() {
        whenever(preferences.isStopBuildPromptShown).thenReturn(true)
        prompts.resumed(loadedOverview)
        verifyNoInteractions(handler)
    }

    @Test fun pauseOrHiddenViewCleanupCancelsTheDelayedPromptWithoutMarkingItShown() {
        prompts.resumed(loadedOverview)
        val pending = argumentCaptor<Runnable>()
        verify(handler).postDelayed(pending.capture(), eq(500L))
        prompts.dispose()
        verify(handler).removeCallbacks(pending.firstValue)
        verify(preferences, never()).saveStopBuildPromptShown()
        verifyNoInteractions(activity)
    }

    @Test fun statusChangeCancelsThePendingOldStatusBeforeSchedulingTheCurrentOne() {
        prompts.resumed(loadedOverview)
        val first = argumentCaptor<Runnable>()
        verify(handler).postDelayed(first.capture(), eq(500L))
        prompts.resumed(loadedOverview.copy(state = "finished"))
        verify(handler).removeCallbacks(first.firstValue)
        verify(handler, times(2)).postDelayed(any(), eq(500L))
    }
}
