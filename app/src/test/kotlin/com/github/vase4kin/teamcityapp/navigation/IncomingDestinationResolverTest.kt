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
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
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
        assertEquals(SplashNavigationDestination, resolver.destination(intent(IncomingDestinationResolver.SPLASH_COMPONENT)))
        verifyNoInteractions(registry)
    }

    @Test fun explicitTypedPayloadTakesPrecedenceOverTheInstalledComponent() {
        whenever(registry.decodeDestination("typed-about")).thenReturn(AboutDestination)
        val incoming = intent(IncomingDestinationResolver.SETTINGS_COMPONENT).putExtra(MainActivity.EXTRA_DESTINATION, "typed-about")
        assertEquals(AboutDestination, resolver.destination(incoming))
        verify(registry).decodeDestination("typed-about")
    }

    @Test fun launcherRootAndUnknownComponentsLeaveStartupSelectionToTheAccountCoordinator() {
        assertNull(resolver.destination(Intent()))
        assertNull(resolver.destination(intent(MainActivity::class.java.name)))
        assertNull(resolver.destination(intent("teamcityapp.features.splash.view.SplashActivity")))
        assertNull(resolver.destination(intent("unknown.component")))
        verifyNoInteractions(registry)
    }

    @Test fun installedAccountAndTestComponentsRetainTheirArguments() {
        assertEquals(ManageAccountsDestination, resolver.destination(intent(IncomingDestinationResolver.MANAGE_ACCOUNTS_COMPONENT)))
        assertEquals(TestDetailsDestination("/occurrence?id=1"), resolver.destination(intent(IncomingDestinationResolver.TEST_DETAILS_COMPONENT).putExtra("arg_test_url", "/occurrence?id=1")))
        assertEquals(TestDetailsDestination(""), resolver.destination(intent(IncomingDestinationResolver.TEST_DETAILS_COMPONENT)))
    }

    @Test fun changeAliasKeepsCompletePlainArgumentsIncludingFileTypesAndBrowserUrls() {
        val incoming = intent(IncomingDestinationResolver.CHANGE_DETAILS_COMPONENT)
            .putExtra("change:id", "123 &456")
            .putExtra("change:comment", " comment ")
            .putExtra("change:user", "user")
            .putExtra("change:date", "date")
            .putStringArrayListExtra("change:file_names", arrayListOf("path + Test.kt", "second"))
            .putStringArrayListExtra("change:file_types", arrayListOf("edited", "added"))
            .putExtra("change:revision", "revision")
            .putExtra("change:web_url", "https://server/change")
        assertEquals(ChangeDetailsDestination(ChangeDetails("123 &456", " comment ", "user", "date", listOf(ChangedFile("path + Test.kt", "edited"), ChangedFile("second", "added")), "revision", "https://server/change")), resolver.destination(incoming))
    }

    @Test fun malformedChangeAliasesRemainInvalidInsteadOfOpeningStartupOrPartialContent() {
        val incoming = intent(IncomingDestinationResolver.CHANGE_DETAILS_COMPONENT)
        assertEquals(ChangeDetailsDestination(), resolver.destination(incoming))
        incoming.putExtra("change:id", "id").putStringArrayListExtra("change:file_names", arrayListOf("file"))
        assertEquals(ChangeDetailsDestination(), resolver.destination(incoming))
        incoming.putStringArrayListExtra("change:file_types", arrayListOf())
        assertEquals(ChangeDetailsDestination(), resolver.destination(incoming))
        incoming.putStringArrayListExtra("change:file_names", arrayListOf())
        assertEquals(ChangeDetailsDestination(ChangeDetails("id", "", "", "", emptyList(), "", "")), resolver.destination(incoming))
    }
}
