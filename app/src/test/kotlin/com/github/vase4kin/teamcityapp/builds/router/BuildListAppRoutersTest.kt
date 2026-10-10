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

package com.github.vase4kin.teamcityapp.builds.router

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.router.AppBuildHistoryRouter
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.AppSnapshotDependenciesRouter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.libraries.builds.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class BuildListAppRoutersTest {
    private val mapper = AppBuildLaunchMapper()
    private val build = BuildLaunchData(
        "1",
        "/builds/1",
        tests = BuildTests("/tests", failed = 2, count = 2),
        properties = BuildProperties(emptyList()),
        snapshotDependencies = BuildCollectionLink("/snapshots", 0)
    )

    @Test fun historyPreservesCompleteBuildAndStandardTaskFlags() {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            AppBuildHistoryRouter(mapper).openBuild(activity, build, "Configuration")
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(BuildDetailsActivity::class.java.name, intent.component!!.className)
            assertEquals("Configuration", intent.getStringExtra(BundleExtractorValues.NAME))
            assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
            assertTrue(intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
            assertEquals(build, mapper.toLaunchData(intent.getSerializableExtra(BundleExtractorValues.BUILD, Build::class.java)!!))
        }
    }

    @Test fun snapshotPreservesCompleteBuildWithinTheCurrentTask() {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            AppSnapshotDependenciesRouter(activity, mapper, mock()).openBuild(build, null)
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(BuildDetailsActivity::class.java.name, intent.component!!.className)
            assertEquals(0, intent.flags and (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            assertTrue(intent.hasExtra(BundleExtractorValues.NAME))
            assertNull(intent.getStringExtra(BundleExtractorValues.NAME))
            assertEquals(build, mapper.toLaunchData(intent.getSerializableExtra(BundleExtractorValues.BUILD, Build::class.java)!!))
        }
    }

    @Test fun snapshotConfigurationUsesTheFeatureHistoryEntryPoint() {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            val history = mock<BuildHistoryNavigation>()
            AppSnapshotDependenciesRouter(activity, mapper, history).openBuildHistory("top-level-id", "Configuration")
            verify(history).open(activity, "top-level-id", "Configuration", null)
            assertNull(shadowOf(activity).nextStartedActivity)
        }
    }
}
