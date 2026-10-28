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

package teamcityapp.features.about.impl

import kotlin.test.*

class AboutActionsTest {
    @Test fun everyAboutActionKeepsItsDestinationOrNativeCapability() {
        val urls = mutableListOf<String>()
        var rates = 0
        var libraries = 0
        AboutAction.entries.forEach { performAboutAction(it, urls::add, { rates++ }, { libraries++ }) }
        assertEquals(1, rates)
        assertEquals(1, libraries)
        assertEquals(listOf("https://github.com/vase4kin/TeamCityApp/issues/new/choose", "https://github.com/vase4kin/TeamCityApp", "https://teamcity.app", "mailto:contact@teamcity.app?subject=Questions%20about%20TeamCityApp", "https://telegra.ph/TeamCityApp-Private-Policy-09-18"), urls)
    }
}
