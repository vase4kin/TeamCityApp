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

package com.github.vase4kin.teamcityapp.build_details.data

import android.app.Application
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.overview.data.*
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildDetailsRunBuildInteractorTest {
    @Test fun nativeRestartResolvesLatestConfigurationAndPassesExactCurrentBranchAndProperties() {
        val arguments = BuildDetailsArguments(overviewArguments())
        val mapper = AppBuildLaunchMapper()
        val delegate = mock<RunBuildInteractor>()
        val created = mutableListOf<String>()
        val interactor = BuildDetailsRunBuildInteractor(arguments) {
            created.add(it)
            delegate
        }
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        val callback = mock<LoadingListenerWithForbiddenSupport<String>>()
        interactor.queueBuild(arguments.current.branchName, arguments.current.properties, callback)
        assertEquals(listOf("latest-configuration"), created)
        verify(delegate).queueBuild(eq("latest-branch"), same(arguments.current.properties), same(callback))
    }

    @Test fun configurationChangeReleasesOldDelegateAndUsesTheUpdatedConfiguration() {
        val arguments = BuildDetailsArguments(overviewArguments())
        val old = mock<RunBuildInteractor>()
        val latest = mock<RunBuildInteractor>()
        val created = mutableListOf<String>()
        val interactor = BuildDetailsRunBuildInteractor(arguments) {
            created.add(it)
            if (it == "old-configuration") old else latest
        }
        val callback = mock<LoadingListenerWithForbiddenSupport<String>>()
        interactor.queueBuild(arguments.current.branchName, arguments.current.properties, callback)
        arguments.publishLoaded(AppBuildLaunchMapper().toLegacyBuild(loadedOverview))
        interactor.queueBuild(arguments.current.branchName, arguments.current.properties, callback)
        assertEquals(listOf("old-configuration", "latest-configuration"), created)
        verify(old).unsubscribe()
        verify(latest).queueBuild(eq("latest-branch"), same(arguments.current.properties), same(callback))
    }

    @Test fun sameConfigurationReusesDelegateAndHostDisposalReleasesSubscriptions() {
        val arguments = BuildDetailsArguments(overviewArguments())
        val delegate = mock<RunBuildInteractor>()
        var created = 0
        val interactor = BuildDetailsRunBuildInteractor(arguments) {
            created++
            delegate
        }
        val callback = mock<LoadingListenerWithForbiddenSupport<String>>()
        interactor.queueBuild(null, null, callback)
        interactor.queueBuild("another", null, callback)
        assertEquals(1, created)
        interactor.unsubscribe()
        verify(delegate).unsubscribe()
    }
}
