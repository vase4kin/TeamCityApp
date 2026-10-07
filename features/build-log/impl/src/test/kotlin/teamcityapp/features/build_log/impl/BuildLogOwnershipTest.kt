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

package teamcityapp.features.build_log.impl
import androidx.fragment.app.FragmentFactory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_log.impl.navigation.BuildLogNavigationImpl
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class BuildLogOwnershipTest {
    @Test fun navigationPassesTheExistingBuildIdAndSupportsFragmentRestoration() {
        val original = BuildLogNavigationImpl().create("build-42")
        assertEquals("build-42", original.requireArguments().getString("buildId"))
        val restored = FragmentFactory().instantiate(original.javaClass.classLoader!!, original.javaClass.name)
        restored.arguments = original.arguments
        assertTrue(restored is BuildLogFragment)
        assertEquals("build-42", restored.requireArguments().getString("buildId"))
    }
}
