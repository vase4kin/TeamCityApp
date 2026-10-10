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

package teamcityapp.features.tests.impl.navigation

import android.os.Bundle
import javax.inject.Inject
import teamcityapp.features.tests.api.TestsNavigation
import teamcityapp.features.tests.impl.TestsFragment

class TestsNavigationImpl @Inject constructor() : TestsNavigation {
    override fun createFragment(url: String, passed: Int, failed: Int, ignored: Int) = TestsFragment().apply {
        arguments = Bundle().apply {
            putString(TestsFragment.ARG_URL, url)
            putInt(TestsFragment.ARG_PASSED, passed)
            putInt(TestsFragment.ARG_FAILED, failed)
            putInt(TestsFragment.ARG_IGNORED, ignored)
        }
    }
}
