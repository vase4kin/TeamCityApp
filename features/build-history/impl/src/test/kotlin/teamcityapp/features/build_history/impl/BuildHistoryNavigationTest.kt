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

package teamcityapp.features.build_history.impl

import android.app.Activity
import android.app.Application
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_history.impl.navigation.BuildHistoryNavigationImpl
import teamcityapp.libraries.navigation.DestinationLauncher

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildHistoryNavigationTest {
    @Test fun incomingNavigationPreservesArgumentsAndDispatchesATypedRootDestination() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val activity = controller.get()
            var received: NavKey? = null
            val launcher = object : DestinationLauncher {
                override fun open(owner: Activity, destination: NavKey, flags: Int) {
                    assertSame(activity, owner)
                    received = destination
                    assertEquals(0, flags)
                }
            }
            BuildHistoryNavigationImpl(launcher).open(activity, "configuration", "Build Android", "personal:false,pinned:false")
            assertEquals(BuildHistoryDestination("configuration", "Build Android", "personal:false,pinned:false"), received)
        } finally {
            controller.pause().stop().destroy()
        }
    }
}
