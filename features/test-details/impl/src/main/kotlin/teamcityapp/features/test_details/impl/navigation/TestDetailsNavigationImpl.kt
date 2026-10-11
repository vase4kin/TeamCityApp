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

package teamcityapp.features.test_details.impl.navigation

import android.app.Activity
import javax.inject.Inject
import teamcityapp.features.test_details.api.TestDetailsNavigation
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.libraries.navigation.DestinationLauncher

class TestDetailsNavigationImpl @Inject constructor(private val launcher: DestinationLauncher) : TestDetailsNavigation {
    override fun open(activity: Activity, url: String) {
        launcher.open(activity, TestDetailsDestination(url))
    }
}
