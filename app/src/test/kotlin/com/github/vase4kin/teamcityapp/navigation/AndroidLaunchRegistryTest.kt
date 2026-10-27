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

package com.github.vase4kin.teamcityapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.home.api.HomeTab
import teamcityapp.features.splash.api.navigation.AccountActivationDestination

class AndroidLaunchRegistryTest {
    private val registry = androidLaunchRegistry()

    @Test fun installedEncodedDestinationsKeepTheirQualifiedNamesAndArguments() {
        assertEquals(AboutDestination, registry.decodeDestination("""{"type":"teamcityapp.features.about.api.navigation.AboutDestination"}"""))
        assertEquals(
            BuildHistoryDestination("bt", "Build", "branch:feature/a"),
            registry.decodeDestination("""{"type":"teamcityapp.features.build_history.api.navigation.BuildHistoryDestination","configurationId":"bt","configurationName":"Build","locator":"branch:feature/a"}""")
        )
    }

    @Test fun accountActivationPreservesItsNestedDestinationWithoutAnAndroidRenderer() {
        val destination = AccountActivationDestination(resetSession = true, destination = HomeDestination(HomeTab.Running))
        assertEquals(destination, registry.decodeDestination(registry.encodeDestination(destination)))
    }
}
