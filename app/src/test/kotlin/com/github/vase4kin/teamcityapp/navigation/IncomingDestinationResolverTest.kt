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
import teamcityapp.features.artifacts.api.ArtifactsNavigation
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.run_build.api.navigation.RunBuildDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.navigation.DestinationRegistry

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class IncomingDestinationResolverTest {
    private val registry = mock<DestinationRegistry>()
    private val codec = mock<BuildLaunchCodec>()
    private val filters = mock<BuildHistoryFilterAdapter>()
    private val resolver = IncomingDestinationResolver(registry, codec, filters)
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

    @Test fun bothInstalledAuthenticationComponentNamesDispatchToTheRootKeys() {
        listOf(IncomingDestinationResolver.LOGIN_COMPONENT, IncomingDestinationResolver.LEGACY_LOGIN_COMPONENT).forEach { component ->
            assertEquals(LoginDestination, resolver.destination(intent(component)))
        }
        listOf(IncomingDestinationResolver.CREATE_ACCOUNT_COMPONENT, IncomingDestinationResolver.LEGACY_CREATE_ACCOUNT_COMPONENT).forEach { component ->
            assertEquals(CreateAccountDestination, resolver.destination(intent(component)))
        }
        verifyNoInteractions(registry)
    }

    @Test fun bothProjectAliasesPreserveNestedArgumentsAndUpBehavior() {
        listOf(IncomingDestinationResolver.PROJECT_COMPONENT, NavigationNavigation.LEGACY_ACTIVITY).forEach { component ->
            assertEquals(ProjectDestination("nested", "Nested project", false), resolver.destination(intent(component).putExtra("id", "nested").putExtra("name", "Nested project")))
            assertEquals(ProjectDestination("_Root", "", false), resolver.destination(intent(component)))
        }
    }

    @Test fun historyAliasesNormalizeTheLegacyFilterAndRespectExplicitLocatorPrecedence() {
        whenever(filters.locator("legacy-filter")).thenReturn("state:queued,branch:raw")
        listOf(IncomingDestinationResolver.HISTORY_COMPONENT, BuildHistoryNavigation.LEGACY_ACTIVITY).forEach { component ->
            val incoming = intent(component).putExtra("id", "configuration").putExtra("name", "Configuration").putExtra("filter", "legacy-filter")
            val destination = BuildHistoryDestination("configuration", "Configuration", "state:queued,branch:raw")
            assertEquals(destination, resolver.destination(incoming))
            whenever(registry.encodeDestination(destination)).thenReturn("plain-history")
            val normalized = resolver.normalizedIntent(incoming, destination)
            assertEquals(incoming.component, normalized.component)
            assertFalse(normalized.hasExtra("filter"))
            assertTrue(incoming.hasExtra("filter"))
            assertEquals("plain-history", normalized.getStringExtra(MainActivity.EXTRA_DESTINATION))
            incoming.putExtra(BuildHistoryNavigation.EXTRA_LOCATOR, "explicit")
            assertEquals(destination.copy(locator = "explicit"), resolver.destination(incoming))
        }
        verify(filters, times(2)).locator("legacy-filter")
    }

    @Test fun artifactAliasesDecodeTheCompleteSnapshotOnceAndNormalizeForRecreation() {
        val build = BuildLaunchData("build", "/build", branchName = "raw/branch")
        whenever(codec.decode("legacy-build")).thenReturn(build)
        val destination = ArtifactsDestination(build, "/children/archive!path", "folder")
        whenever(registry.encodeDestination(destination)).thenReturn("plain-artifacts")
        whenever(registry.decodeDestination("plain-artifacts")).thenReturn(destination)
        listOf(IncomingDestinationResolver.ARTIFACTS_COMPONENT, ArtifactsNavigation.LEGACY_ACTIVITY).forEach { component ->
            val incoming = intent(component).putExtra(ArtifactsNavigation.BUILD, "legacy-build").putExtra(ArtifactsNavigation.URL, destination.href).putExtra(ArtifactsNavigation.NAME, destination.name)
            assertEquals(destination, resolver.destination(incoming))
            val normalized = resolver.normalizedIntent(incoming, destination)
            assertFalse(normalized.hasExtra(ArtifactsNavigation.BUILD))
            assertTrue(incoming.hasExtra(ArtifactsNavigation.BUILD))
            assertEquals(destination, resolver.destination(normalized))
        }
        verify(codec, times(2)).decode("legacy-build")
    }

    @Test fun bothFormAliasesPreserveConfigurationIdsAndStandaloneResultOwnership() {
        listOf(IncomingDestinationResolver.RUN_BUILD_COMPONENT, IncomingDestinationResolver.LEGACY_RUN_BUILD_COMPONENT).forEach { component ->
            assertEquals(RunBuildDestination("configuration"), resolver.destination(intent(component).putExtra("BuildTypeId", "configuration")))
            assertEquals(RunBuildDestination(""), resolver.destination(intent(component)))
        }
        listOf(IncomingDestinationResolver.FILTER_BUILDS_COMPONENT, IncomingDestinationResolver.LEGACY_FILTER_BUILDS_COMPONENT).forEach { component ->
            assertEquals(FilterBuildsDestination("configuration"), resolver.destination(intent(component).putExtra("BuildTypeId", "configuration")))
            assertEquals(FilterBuildsDestination(""), resolver.destination(intent(component)))
        }
        verifyNoInteractions(codec, filters, registry)
    }
}
