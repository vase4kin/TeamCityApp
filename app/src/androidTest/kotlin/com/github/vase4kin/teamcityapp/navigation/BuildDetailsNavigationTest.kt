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

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.router.ComposeArtifactsPlatformModule
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import dagger.hilt.android.testing.*
import io.reactivex.Single
import java.io.File
import javax.inject.Inject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.navigation.DestinationLauncher

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@UninstallModules(ComposeArtifactsPlatformModule::class)
@RunWith(AndroidJUnit4::class)
class BuildDetailsNavigationTest {
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

    @Inject lateinit var launcher: DestinationLauncher
    private val mapper = AppBuildLaunchMapper()
    private val opened = mutableListOf<DownloadedArtifact>()
    private val browser = mutableListOf<String>()

    @BindValue @JvmField
    val platforms: ArtifactsPlatformRouterFactory = object : ArtifactsPlatformRouterFactory {
        override fun create(activity: Activity): ArtifactsAppRouter = object : ArtifactsAppRouter {
            override fun permission(activity: Activity, file: ArtifactDownload) = ArtifactPermission.Allowed
            override fun openDownloadedFile(activity: Activity, file: DownloadedArtifact) {
                assertTrue(activity is MainActivity)
                assertTrue(File(file.path).isFile)
                opened += file
            }
            override fun openBrowser(activity: Activity, build: BuildLaunchData, href: String) {
                assertTrue(activity is MainActivity)
                browser += href
            }
            override fun dispose(activity: Activity) {}
        }
    }
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        TestUtils.disableOnboarding()
        app.appInjector.cacheManager().evictAllCache()
        val storage = app.appInjector.sharedUserStorage()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java))
    private fun awaitTag(tag: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(tag).assertIsDisplayed()
    }
    private fun awaitText(text: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun open(scenario: ActivityScenario<MainActivity>, build: BuildLaunchData) {
        awaitTag("navigation:row:project:id")
        scenario.onActivity { launcher.open(it, BuildDetailsDestination(build, "Fallback configuration")) }
        awaitTag("overview:list")
    }
    private fun tab(name: String) {
        compose.onNodeWithTag("details:tab:$name").performScrollTo().performClick()
    }

    @Test fun incomingTabsAndParametersRemainStableWhenOverviewLoadsADifferentSnapshot() {
        val incoming = mapper.toLaunchData(Mocks.failedBuild()).copy(number = "incoming", properties = BuildProperties(listOf(BuildProperty("initial.parameter", "initial-value"))), snapshotDependencies = BuildCollectionLink(null))
        val loaded = incoming.copy(number = "latest", changes = null, tests = null, artifacts = null, snapshotDependencies = null, properties = BuildProperties(listOf(BuildProperty("loaded.parameter", "loaded-value"))))
        doReturn(Single.just(mapper.toLegacyBuild(loaded))).`when`(service).build(incoming.href)
        launch().use { scenario ->
            open(scenario, incoming)
            awaitText("#latest")
            tab("Parameters")
            awaitTag("properties:list")
            compose.onNodeWithText("initial.parameter").assertIsDisplayed()
            compose.onNodeWithText("loaded.parameter").assertDoesNotExist()
            scenario.recreate()
            awaitTag("properties:list")
            compose.onNodeWithTag("details:tab:Parameters").assertIsSelected()
            tab("Changes")
            awaitTag("changes:list")
            tab("Tests")
            awaitTag("tests:list")
            tab("BuildLog")
            awaitTag("build-log:screen")
            tab("Artifacts")
            awaitTag("artifacts:list")
            tab("Snapshots")
            awaitTag("snapshot:screen")
            tab("Overview")
            awaitTag("overview:list")
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
            pressBack()
            awaitTag("navigation:row:project:id")
        }
    }

    @Test fun branchSheetRestoresAndNavigatesUsingTheLoadedConfigurationAndExactBranch() {
        val incoming = mapper.toLaunchData(Mocks.successBuild())
        val loaded = incoming.copy(buildTypeId = "latest-configuration", branchName = "refs/heads/opaque:branch", configuration = incoming.configuration?.copy(name = "Latest configuration"))
        doReturn(Single.just(mapper.toLegacyBuild(loaded))).`when`(service).build(incoming.href)
        launch().use { scenario ->
            open(scenario, incoming)
            compose.onNodeWithTag("overview:row:Branch").performClick()
            awaitTag("sheet:Branch")
            scenario.recreate()
            awaitTag("sheet:Branch")
            compose.onNodeWithTag("sheet:Branch").performClick()
            awaitTag("history:list")
            val locator = BuildListFilterImpl().apply { setBranch(loaded.branchName.orEmpty()) }.toLocator()
            compose.waitUntil(10_000) { runCatching { verify(service).listBuilds("latest-configuration", locator) }.isSuccess }
            pressBack()
            awaitTag("overview:list")
            compose.onNodeWithTag("sheet:content").assertDoesNotExist()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun artifactFolderAndDownloadSheetKeepTheRightOwnerThroughRecreation() {
        val build = mapper.toLaunchData(Mocks.successBuild())
        val folderHref = "/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"
        val fileHref = "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/AndroidManifest.xml"
        doReturn(Single.just(Mocks.artifacts())).`when`(service).listArtifacts(folderHref, "browseArchives:true")
        launch().use { scenario ->
            open(scenario, build)
            tab("Artifacts")
            awaitTag("artifacts:list")
            compose.onNodeWithTag("artifacts:row:/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res").performClick()
            awaitTag("artifacts:list")
            awaitText("res")
            scenario.recreate()
            awaitTag("artifacts:list")
            compose.onNodeWithTag("artifacts:row:$fileHref").performClick()
            awaitTag("sheet:ArtifactDownload")
            scenario.recreate()
            awaitTag("sheet:ArtifactDownload")
            compose.onNodeWithTag("sheet:ArtifactDownload").performClick()
            compose.waitUntil(15_000) { opened.size == 1 }
            scenario.recreate()
            awaitTag("artifacts:list")
            assertEquals(1, opened.size)
            pressBack()
            awaitTag("artifacts:list")
            compose.onNodeWithTag("details:tab:Artifacts").assertIsSelected()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun stoppingBuildSurvivesConfirmationRecreationAndReplacesTheOldTabOwners() {
        val running = mapper.toLaunchData(Mocks.runningBuild())
        val cancelled = running.copy(state = "finished", number = "cancelled", artifacts = BuildCollectionLink("/guestAuth/app/rest/builds/id:92912/artifacts/children/"))
        var current = running
        doAnswer { Single.just(mapper.toLegacyBuild(current)) }.`when`(service).build(running.href)
        doAnswer {
            current = cancelled
            Single.just(mapper.toLegacyBuild(cancelled))
        }.`when`(service).cancelBuild(running.href, BuildCancelRequest(true))
        launch().use { scenario ->
            open(scenario, running)
            compose.onNodeWithContentDescription("More actions").performClick()
            compose.onNodeWithText("Stop").performClick()
            awaitTag("details:confirm")
            scenario.recreate()
            awaitTag("details:confirm")
            compose.onNodeWithTag("details:readd").performClick()
            compose.onNodeWithTag("details:confirm").performClick()
            awaitText("#cancelled")
            compose.onNodeWithTag("details:tab:Overview").assertIsSelected()
            tab("Artifacts")
            awaitTag("artifacts:list")
            verify(service, times(1)).cancelBuild(running.href, BuildCancelRequest(true))
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun restartSubmitsOnceThenQueuedBuildReplacesTheFinishedDestination() {
        val build = mapper.toLaunchData(Mocks.successBuild())
        launch().use { scenario ->
            open(scenario, build)
            compose.onNodeWithContentDescription("More actions").performClick()
            compose.onNodeWithText("Restart").performClick()
            awaitTag("details:confirm")
            scenario.recreate()
            awaitTag("details:confirm")
            compose.onNodeWithTag("details:confirm").performClick()
            awaitText("Build is restarted successfully")
            compose.onNodeWithText("Show").performClick()
            awaitTag("overview:row:WaitReason")
            compose.onNodeWithTag("details:tab:BuildLog").assertDoesNotExist()
            compose.onNodeWithTag("details:tab:Artifacts").assertDoesNotExist()
            scenario.recreate()
            awaitTag("overview:row:WaitReason")
            verify(service, times(1)).queueBuild(org.mockito.kotlin.any())
            pressBack()
            awaitTag("navigation:row:project:id")
        }
    }

    @Test fun repeatedBuildVisitsHaveIndependentSelectedTabsAndBackRestoresTheFirst() {
        val build = mapper.toLaunchData(Mocks.failedBuild())
        launch().use { scenario ->
            open(scenario, build)
            tab("Tests")
            awaitTag("tests:list")
            scenario.onActivity { launcher.open(it, BuildDetailsDestination(build)) }
            awaitTag("overview:list")
            compose.onNodeWithTag("details:tab:Overview").assertIsSelected()
            scenario.recreate()
            awaitTag("overview:list")
            pressBack()
            awaitTag("tests:list")
            compose.onNodeWithTag("details:tab:Tests").assertIsSelected()
            pressBack()
            awaitTag("navigation:row:project:id")
        }
    }
}
