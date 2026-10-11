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

package teamcityapp.features.snapshot_dependencies.impl

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesAppRouter
import teamcityapp.features.snapshot_dependencies.impl.navigation.SnapshotDependenciesNavigationImpl
import teamcityapp.features.snapshot_dependencies.impl.router.SnapshotDependenciesRouterImpl
import teamcityapp.libraries.builds.BuildLaunchData

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SnapshotDependenciesNavigationTest {
    @Test fun idOnlyEntryPreservesOldBundleKey() {
        val fragment = SnapshotDependenciesNavigationImpl().createFragment("123")
        assertTrue(fragment is SnapshotDependenciesFragment)
        assertEquals("123", fragment.requireArguments().getString("id"))
        assertFalse(fragment.requireArguments().containsKey("name"))
    }

    @Test fun optionalNameUsesOldBundleKey() {
        val fragment = SnapshotDependenciesNavigationImpl().createFragment("123", "Debug")
        assertEquals("Debug", fragment.requireArguments().getString("name"))
    }

    @Test fun legacyBundleWithoutNameUsesEmptyFallback() {
        val repository = object : teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository {
            override suspend fun dependencies(buildId: String, forceRefresh: Boolean) = emptyList<BuildLaunchData>()
        }
        val vm = SnapshotDependenciesViewModel(repository, SavedStateHandle(mapOf("id" to "123")))
        assertEquals("", vm.buildTypeName)
    }

    @Test fun routerPassesCompleteSnapshotAndFallbackToAppBoundary() {
        var actual: BuildLaunchData? = null
        var fallback: String? = null
        var history: Pair<String, String>? = null
        val router = SnapshotDependenciesRouterImpl(object : SnapshotDependenciesAppRouter {
            override fun openBuild(build: BuildLaunchData, buildTypeName: String?) {
                actual = build
                fallback = buildTypeName
            }
            override fun openBuildHistory(configurationId: String, configurationName: String) {
                history = configurationId to configurationName
            }
        })
        val build = snapshotBuild().copy(personal = true, pinned = true, cleanSources = true, queueAtTop = true)
        router.openBuild(build, "fallback")
        assertSame(build, actual)
        assertEquals("fallback", fallback)
        router.openBuildHistory("config", "Debug")
        assertEquals("config" to "Debug", history)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankBuildIdRejectedAtNavigationBoundary() {
        SnapshotDependenciesNavigationImpl().createFragment(" ")
    }
}
