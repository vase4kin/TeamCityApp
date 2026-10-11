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

package teamcityapp.features.settings.impl.navigation

import android.app.Activity
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.navigation.DestinationLauncher

class SettingsDestinationTest {
    @Test fun legacyEntryDelegatesToTheRootDestinationLauncher() {
        val owner = mock(Activity::class.java)
        val requests = mutableListOf<Pair<Activity, NavKey>>()
        val launcher = object : DestinationLauncher {
            override fun open(activity: Activity, destination: NavKey, flags: Int) {
                requests += activity to destination
            }
        }
        SettingsNavigationImpl(launcher).open(owner)
        assertEquals(listOf(owner to SettingsDestination), requests)
    }
}
