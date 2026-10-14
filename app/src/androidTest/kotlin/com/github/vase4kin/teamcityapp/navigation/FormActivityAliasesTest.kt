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
package com.github.vase4kin.teamcityapp.navigation

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavKey
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.runbuild.api.Branches
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination
import teamcityapp.features.run_build.api.navigation.RunBuildDestination
import teamcityapp.libraries.theme.R as ThemeR

/** Private installed entry points keep complete arguments and root-owned form state. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FormActivityAliasesTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockito = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val api = HiltApiTestRule(hilt) { service }

    @JvmField
    @Rule(order = 3)
    val compose = createEmptyComposeRule()

    @Spy private val service: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val app get() = context.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        doReturn(Single.just(Branches(listOf(Branch("main"), Branch("release"))))).`when`(service).listBranches(org.mockito.kotlin.any())
    }

    @Test fun bothRunBuildAliasesRetainTheConfigurationAndDraftAndCloseTheirRoot() {
        listOf(IncomingDestinationResolver.RUN_BUILD_COMPONENT, IncomingDestinationResolver.LEGACY_RUN_BUILD_COMPONENT).forEach { component ->
            verifyAlias(component, "run-configuration", RunBuildDestination("run-configuration"))
        }
        verify(service, times(2)).listBranches("run-configuration")
    }

    @Test fun bothFilterBuildsAliasesRetainTheConfigurationAndDraftAndCloseTheirRoot() {
        listOf(IncomingDestinationResolver.FILTER_BUILDS_COMPONENT, IncomingDestinationResolver.LEGACY_FILTER_BUILDS_COMPONENT).forEach { component ->
            verifyAlias(component, "filter-configuration", FilterBuildsDestination("filter-configuration"))
        }
        verify(service, times(2)).listBranches("filter-configuration")
    }

    private fun verifyAlias(component: String, configurationId: String, destination: NavKey) {
        ActivityScenario.launch<MainActivity>(Intent().setClassName(context.packageName, component).putExtra("BuildTypeId", configurationId)).use { scenario ->
            awaitBranch()
            compose.onNodeWithTag("branches:input").performTextClearance()
            compose.onNodeWithTag("branches:input").performTextInput("retained/raw branch")
            scenario.recreate()
            awaitBranch()
            compose.onNodeWithTag("branches:input").assertTextEquals("retained/raw branch")
            scenario.onActivity {
                assertEquals(component, it.intent.component!!.className)
                assertEquals(destination, it.navigationSnapshot!!.entries.single().destination)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
            compose.onNodeWithContentDescription(context.getString(ThemeR.string.action_back)).performClick()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    private fun awaitBranch() = compose.waitUntil(10_000) { compose.onAllNodesWithTag("branches:input").fetchSemanticsNodes().isNotEmpty() }
}
