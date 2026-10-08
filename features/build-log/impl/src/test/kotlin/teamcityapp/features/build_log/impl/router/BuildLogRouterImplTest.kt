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

package teamcityapp.features.build_log.impl.router

import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs

class BuildLogRouterImplTest {
    @Test
    fun customTabsCanBeBoundForEachViewLifetime() {
        val tabs = mock(ChromeCustomTabs::class.java)
        val router = BuildLogRouterImpl(tabs)
        verifyNoInteractions(tabs)

        router.initCustomsTabs()
        router.openUrl("https://teamcity.example/build/1")
        router.unbindCustomsTabs()
        router.initCustomsTabs()
        router.openUrl("https://teamcity.example/build/1")
        router.unbindCustomsTabs()

        inOrder(tabs).apply {
            verify(tabs).initCustomsTabs()
            verify(tabs).launchUrl("https://teamcity.example/build/1")
            verify(tabs).unbindCustomsTabs()
            verify(tabs).initCustomsTabs()
            verify(tabs).launchUrl("https://teamcity.example/build/1")
            verify(tabs).unbindCustomsTabs()
        }
    }
}
