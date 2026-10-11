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

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.navigation.DestinationRegistry

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class IncomingDestinationResolverTest {
    private val registry = mock<DestinationRegistry>()
    private val resolver = IncomingDestinationResolver(registry)
    private fun intent(component: String) = Intent().setComponent(ComponentName("application", component))

    @Test fun installedFeatureComponentsMapToTheirDestinationKeys() {
        assertEquals(AboutDestination, resolver.destination(intent(IncomingDestinationResolver.ABOUT_COMPONENT)))
        assertEquals(SettingsDestination, resolver.destination(intent(IncomingDestinationResolver.SETTINGS_COMPONENT)))
        verifyNoInteractions(registry)
    }

    @Test fun explicitTypedPayloadTakesPrecedenceOverTheInstalledComponent() {
        whenever(registry.decodeDestination("typed-about")).thenReturn(AboutDestination)
        val incoming = intent(IncomingDestinationResolver.SETTINGS_COMPONENT).putExtra(MainActivity.EXTRA_DESTINATION, "typed-about")
        assertEquals(AboutDestination, resolver.destination(incoming))
        verify(registry).decodeDestination("typed-about")
    }

    @Test fun LauncherRootAndUnknownComponentsLeaveStartupSelectionToTheAccountCoordinator() {
        assertNull(resolver.destination(Intent()))
        assertNull(resolver.destination(intent(MainActivity::class.java.name)))
        assertNull(resolver.destination(intent("teamcityapp.features.splash.view.SplashActivity")))
        assertNull(resolver.destination(intent("unknown.component")))
        verifyNoInteractions(registry)
    }
}
