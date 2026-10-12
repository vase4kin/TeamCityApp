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

import android.app.Application
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.data.DefaultBuildDetailsMetadataRepository
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppBuildDetailsMetadataRepositoryTest {
    private val legacy = mock<Repository>()
    private val storage = mock<SharedUserStorage>()
    private val onboarding = mock<OnboardingManager>()
    private var active = UserAccount("https://same-server", "alice", byteArrayOf(), false, true)
    private val build = BuildLaunchData("42", "/b", changes = BuildCollectionLink("/changes?opaque:42"), tests = BuildTests("/tests?opaque:42", 1, 2, 3, 6))
    private suspend fun repository(scope: TestScope) = DefaultBuildDetailsMetadataRepository(Provider { legacy }, storage, onboarding, StandardTestDispatcher(scope.testScheduler)).also {
        whenever(storage.activeUser).thenAnswer { active }
        whenever(onboarding.isShareBuildPromptShown()).thenReturn(false)
        whenever(onboarding.isOpenBuildInBrowserPromptShown()).thenReturn(false)
    }

    @Test fun independentCountsPreserveOpaqueUrlsAndInstalledForceRefreshPolicy() = runTest {
        val repository = repository(this)
        whenever(legacy.listChanges("/changes?opaque:42,count:2147483647&fields=count", true)).thenReturn(Single.just(Changes(emptyList(), 7)))
        whenever(legacy.listTestOccurrences("/tests?opaque:42,count:2147483647&fields=count", true)).thenReturn(Single.just(TestOccurrences(12)))
        assertEquals(7, repository.count(build, BuildDetailsSection.Changes))
        assertEquals(12, repository.count(build, BuildDetailsSection.Tests))
        verify(legacy).listChanges("/changes?opaque:42,count:2147483647&fields=count", true)
        verify(legacy).listTestOccurrences("/tests?opaque:42,count:2147483647&fields=count", true)
    }

    @Test fun cancelledCountDisposesTheLegacySubscription() = runTest {
        val repository = repository(this)
        var disposed = false
        whenever(legacy.listChanges(any(), eq(true))).thenReturn(Single.never<Changes>().doOnDispose { disposed = true })
        val request = async { repository.count(build, BuildDetailsSection.Changes) }
        runCurrent()
        request.cancel()
        runCurrent()
        assertTrue(disposed)
        assertTrue(request.isCancelled)
    }

    @Test fun sameServerAccountSwitchRejectsBothOldSuccessAndOldFailure() = runTest {
        val repository = repository(this)
        val success = SingleSubject.create<Changes>()
        whenever(legacy.listChanges(any(), eq(true))).thenReturn(success)
        val first = async { repository.count(build, BuildDetailsSection.Changes) }
        runCurrent()
        active = UserAccount("https://same-server", "bob", byteArrayOf(), false, true)
        success.onSuccess(Changes(emptyList(), 9))
        runCurrent()
        assertTrue(first.isCancelled)
        val failure = SingleSubject.create<TestOccurrences>()
        whenever(legacy.listTestOccurrences(any(), eq(true))).thenReturn(failure)
        val second = async { repository.count(build, BuildDetailsSection.Tests) }
        runCurrent()
        active = UserAccount("https://same-server", "carol", byteArrayOf(), false, true)
        failure.onError(IllegalStateException("old account offline"))
        runCurrent()
        assertTrue(second.isCancelled)
    }

    @Test fun educationUsesTheExistingGlobalFlagsAndAcknowledgesOnlyTheDismissedKind() = runTest {
        val repository = repository(this)
        whenever(onboarding.isRestartBuildPromptShown).thenReturn(true)
        assertEquals(setOf(BuildDetailsPrompt.Stop, BuildDetailsPrompt.Remove, BuildDetailsPrompt.Share, BuildDetailsPrompt.Browser), repository.pendingPrompts())
        repository.acknowledgePrompt(BuildDetailsPrompt.Stop)
        verify(onboarding).saveStopBuildPromptShown()
        verify(onboarding, never()).saveRestartBuildPromptShown()
        verify(onboarding, never()).saveRemoveBuildFromQueuePromptShown()
        repository.acknowledgePrompt(BuildDetailsPrompt.Remove)
        repository.acknowledgePrompt(BuildDetailsPrompt.Restart)
        verify(onboarding).saveRemoveBuildFromQueuePromptShown()
        verify(onboarding).saveRestartBuildPromptShown()
        repository.acknowledgePrompt(BuildDetailsPrompt.Share)
        repository.acknowledgePrompt(BuildDetailsPrompt.Browser)
        verify(onboarding).saveShareBuildPromptShown()
        verify(onboarding).saveOpenBuildInBrowserPromptShown()
    }

    @Test fun newActionPreferencesPersistIndependentlyWithoutResettingExistingEducation() = runTest {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences(teamcityapp.libraries.onboarding.OnboardingManagerImpl.PREF_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().putBoolean("StopBuilds", true).putBoolean("RestartBuilds", true).commit()
        val first = teamcityapp.libraries.onboarding.OnboardingManagerImpl(context)
        first.saveShareBuildPromptShown(false)
        first.saveOpenBuildInBrowserPromptShown(false)
        assertFalse(first.isShareBuildPromptShown())
        assertFalse(first.isOpenBuildInBrowserPromptShown())
        first.saveShareBuildPromptShown()
        val restored = teamcityapp.libraries.onboarding.OnboardingManagerImpl(context)
        assertTrue(restored.isShareBuildPromptShown())
        assertFalse(restored.isOpenBuildInBrowserPromptShown())
        restored.saveOpenBuildInBrowserPromptShown()
        val final = teamcityapp.libraries.onboarding.OnboardingManagerImpl(context)
        assertTrue(final.isOpenBuildInBrowserPromptShown())
        assertTrue(final.isStopBuildPromptShown)
        assertTrue(final.isRestartBuildPromptShown)
        assertFalse(final.isRemoveBuildFromQueuePromptShown)
    }
}
