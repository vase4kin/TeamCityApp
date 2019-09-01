/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.artifact.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.azimolabs.conditionwatcher.ConditionWatcher
import com.azimolabs.conditionwatcher.Instruction
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount
import io.reactivex.Single
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.ResponseBody
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import java.util.ArrayList

private const val BUILD_TYPE_NAME = "name"
private const val TIMEOUT = 5000

/**
 * Tests for [ArtifactListFragment]
 */
@RunWith(AndroidJUnit4::class)
class ArtifactListFragmentTest {

    @JvmField
    @Rule
    val restComponentDaggerRule: DaggerMockRule<RestApiComponent> =
        DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
            .addComponentDependency(
                AppComponent::class.java,
                AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
            )
            .set { restApiComponent ->
                val app =
                    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
                app.setRestApiInjector(restApiComponent)
            }

    @JvmField
    @Rule
    val activityRule: CustomIntentsTestRule<BuildDetailsActivity> =
        CustomIntentsTestRule(BuildDetailsActivity::class.java)

    @JvmField
    @Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant("android.permission.WRITE_EXTERNAL_STORAGE")

    @Spy
    private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    @Spy
    private val build: Build = Mocks.successBuild()

    companion object {
        @JvmStatic
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.restApiInjector.sharedUserStorage().clearAll()
        app.restApiInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        ConditionWatcher.setTimeoutLimit(TIMEOUT)
    }

    @Test
    fun testUserCanSeeArtifacts() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking artifacts
        onView(withId(R.id.artifact_recycler_view)).check(hasItemsCount(3))
        onView(
            withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(
                0,
                R.id.itemTitle
            )
        ).check(matches(withText("res")))
        onView(
            withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(
                1,
                R.id.itemTitle
            )
        ).check(matches(withText("AndroidManifest.xml")))
        onView(
            withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(
                1,
                R.id.itemSubTitle
            )
        ).check(matches(withText("7.77 kB")))
        onView(
            withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(
                2,
                R.id.itemTitle
            )
        ).check(matches(withText("index.html")))
        onView(
            withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(
                2,
                R.id.itemSubTitle
            )
        ).check(matches(withText("698 kB")))
    }

    @Test
    fun testUserCanSeeArtifactsEmptyMessageIfArtifactsAreEmpty() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        `when`(
            teamCityService.listArtifacts(
                anyString(),
                anyString()
            )
        ).thenReturn(Single.just(Files(emptyList())))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking message
        onView(withText(R.string.empty_list_message_artifacts)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSeeArtifactsErrorMessageIfSmthBadHappens() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(
            Single.error(
                RuntimeException("Fake error happened!")
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking error
        onView(withText(R.string.error_view_error_text)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanOpenArtifactWithChildren() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val folderOne = File(
            "res",
            File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
            "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
        )
        val folderTwo = File(
            "res_level_deeper1",
            File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
            "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
        )
        val folderThree = File(
            "res_level_deeper2",
            File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
            "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
        )
        val deeperArtifacts = ArrayList<File>()
        deeperArtifacts.add(folderTwo)
        deeperArtifacts.add(folderThree)
        `when`(teamCityService.listArtifacts(anyString(), anyString()))
            .thenReturn(Single.just(Files(listOf(folderOne))))
            .thenReturn(Single.just(Files(deeperArtifacts)))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking first level artifacts
        onView(withId(R.id.artifact_recycler_view)).check(hasItemsCount(1))
        onView(withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .check(matches(withText("res")))

        // Clicking first level artifacts
        onView(withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "The artifact page is not loaded"
            }

            override fun checkCondition(): Boolean {
                var isResFolderClicked = false
                try {
                    onView(withText("res_level_deeper1")).check(matches(isDisplayed()))
                    isResFolderClicked = true
                } catch (ignored: Exception) {
                    onView(withRecyclerView(R.id.artifact_recycler_view).atPosition(0))
                        .perform(click())
                }

                return isResFolderClicked
            }
        })

        // In case of the same recycler view ids
        onView(withText("res_level_deeper1")).check(matches(isDisplayed()))
        onView(withText("res_level_deeper2")).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testUserCanOpenArtifactWithChildrenByLongTap() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val folderOne = File(
            "res",
            File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
            "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
        )
        val folderTwo = File(
            "res_level_deeper1",
            File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
            "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
        )
        val folderThree = File(
            "res_level_deeper2",
            File.Children("/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"),
            "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res"
        )
        val deeperArtifacts = ArrayList<File>()
        deeperArtifacts.add(folderTwo)
        deeperArtifacts.add(folderThree)
        `when`(teamCityService.listArtifacts(anyString(), anyString()))
            .thenReturn(Single.just(Files(listOf(folderOne))))
            .thenReturn(Single.just(Files(deeperArtifacts)))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking first level artifacts
        onView(withId(R.id.artifact_recycler_view)).check(hasItemsCount(1))
        onView(withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .check(matches(withText("res")))

        // Clicking first level artifacts
        onView(withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(0, R.id.itemTitle))
            .perform(longClick())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "The artifact menu is not opened"
            }

            override fun checkCondition(): Boolean {
                var isMenuOpened = false
                try {
                    onView(withText(R.string.artifact_open))
                        .check(matches(isDisplayed()))
                    isMenuOpened = true
                } catch (ignored: Exception) {
                    // Clicking first level artifacts
                    onView(
                        withRecyclerView(R.id.artifact_recycler_view).atPositionOnView(
                            0,
                            R.id.itemTitle
                        )
                    )
                        .perform(longClick())
                }

                return isMenuOpened
            }
        })

        // Click on open option
        onView(withText(R.string.artifact_open))
            .perform(click())

        // In case of the same recycler view ids
        onView(withText("res_level_deeper1")).check(matches(isDisplayed()))
        onView(withText("res_level_deeper2")).check(matches(isDisplayed()))
    }

    @Ignore
    @Test
    fun testUserCanDownloadArtifact() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        `when`(teamCityService.downloadFile(anyString())).thenReturn(
            Single.just(
                ResponseBody.create(
                    null,
                    "text"
                )
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Clicking on artifact to download
        onView(
            withRecyclerView(R.id.artifact_recycler_view)
                .atPositionOnView(1, R.id.itemTitle)
        )
            .check(matches(withText("AndroidManifest.xml")))
            .perform(click())

        // Click on download option
        onView(withText(R.string.artifact_download))
            .perform(click())

        // Check filter builds activity is opened
        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasType("*/*")
            )
        )
    }

    @Ignore("Test opens chrome and gets stuck")
    @Test
    fun testUserCanOpenHtmlFileInBrowser() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Clicking on artifact
        onView(
            withRecyclerView(R.id.artifact_recycler_view)
                .atPositionOnView(2, R.id.itemTitle)
        )
            .check(matches(withText("index.html")))
            .perform(click())

        // Click on download option
        onView(withText(R.string.artifact_open_in_browser))
            .perform(click())

        // Check filter builds activity is opened
        intended(
            allOf(
                hasData(Uri.parse("https://teamcity.server.com/repository/download/Checkstyle_IdeaInspectionsPullRequest/null:id/TCity.apk!/index.html?guest=1")),
                hasAction(Intent.ACTION_VIEW)
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testUserSeeSnackBarWithErrorMessageIfArtifactWasNotDownloaded() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        `when`(teamCityService.downloadFile(anyString())).thenReturn(Single.error(RuntimeException("ERROR!")))

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Checking artifact title
        onView(
            withRecyclerView(R.id.artifact_recycler_view)
                .atPositionOnView(1, R.id.itemTitle)
        )
            .check(matches(withText("AndroidManifest.xml")))
            .perform(click())

        // Clicking on artifact to download
        onView(withText("AndroidManifest.xml")).perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "The artifact menu is not opened"
            }

            override fun checkCondition(): Boolean {
                var isMenuOpened = false
                try {
                    onView(withText(R.string.artifact_download))
                        .check(matches(isDisplayed()))
                    isMenuOpened = true
                } catch (ignored: Exception) {
                    onView(
                        withRecyclerView(R.id.artifact_recycler_view)
                            .atPositionOnView(1, R.id.itemTitle)
                    )
                        .perform(click())
                }

                return isMenuOpened
            }
        })

        // Click on download option
        onView(withText(R.string.artifact_download))
            .perform(click())

        // Checking error snack bar message
        onView(withText(R.string.download_artifact_retry_snack_bar_text))
            .check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun testUserBeingAskedToGrantAllowInstallPackagesPermissions() {
        // Prepare mocks
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        val files = ArrayList<File>()
        files.add(
            File(
                "my-fancy-app.apk",
                697840,
                File.Content("/guestAuth/app/rest/builds/id:92912/artifacts/content/TCity.apk!/my-fancy-app.apk"),
                "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/my-fancy-app.apk"
            )
        )
        val filesMock = Files(files)
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(
            Single.just(
                filesMock
            )
        )

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        val intent = Intent()
        val b = Bundle()
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME)
        intent.putExtras(b)

        // Start activity
        activityRule.launchActivity(intent)

        // Checking artifact tab title
        onView(withText("Artifacts"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())

        // Clicking on apk to download
        onView(
            withRecyclerView(R.id.artifact_recycler_view)
                .atPositionOnView(0, R.id.itemTitle)
        )
            .check(matches(withText("my-fancy-app.apk")))
            .perform(click())

        ConditionWatcher.waitForCondition(object : Instruction() {
            override fun getDescription(): String {
                return "The artifact menu is not opened"
            }

            override fun checkCondition(): Boolean {
                var isMenuOpened = false
                try {
                    onView(withText(R.string.artifact_download))
                        .check(matches(isDisplayed()))
                    isMenuOpened = true
                } catch (ignored: Exception) {
                    onView(
                        withRecyclerView(R.id.artifact_recycler_view)
                            .atPositionOnView(0, R.id.itemTitle)
                    )
                        .perform(click())
                }

                return isMenuOpened
            }
        })

        // Click on download option
        onView(withText(R.string.artifact_download))
            .perform(click())

        // Check dialog text
        onView(withText(R.string.permissions_install_packages_dialog_content)).check(
            matches(
                isDisplayed()
            )
        )

        /*// Confirm dialog
        onView(withText(R.string.dialog_ok_title)).perform(click());

        // Check filter builds activity is opened
        intended(allOf(
                hasAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES),
                hasData("package:com.github.vase4kin.teamcityapp.mock.debug")));*/
    }
}
