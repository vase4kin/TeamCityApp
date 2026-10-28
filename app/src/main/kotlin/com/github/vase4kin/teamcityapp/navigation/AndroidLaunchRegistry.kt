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

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import teamcityapp.libraries.navigation.*

/** Installed Intent and saved-argument codecs. The common app root owns rendering. */
internal fun androidLaunchRegistry(): DestinationRegistry = DestinationRegistry(
    setOf(
        LaunchDestination(teamcityapp.features.splash.api.navigation.AccountActivationDestination::class, teamcityapp.features.splash.api.navigation.AccountActivationDestination.serializer()),
        LaunchDestination(teamcityapp.features.about.api.navigation.AboutDestination::class, teamcityapp.features.about.api.navigation.AboutDestination.serializer()),
        LaunchDestination(teamcityapp.features.agents.api.navigation.AgentsDestination::class, teamcityapp.features.agents.api.navigation.AgentsDestination.serializer()),
        LaunchDestination(teamcityapp.features.artifacts.api.navigation.ArtifactsDestination::class, teamcityapp.features.artifacts.api.navigation.ArtifactsDestination.serializer()),
        LaunchDestination(teamcityapp.features.bottom_sheet.api.navigation.ActionSheetDestination::class, teamcityapp.features.bottom_sheet.api.navigation.ActionSheetDestination.serializer()),
        LaunchDestination(teamcityapp.features.build_details.api.BuildDetailsDestination::class, teamcityapp.features.build_details.api.BuildDetailsDestination.serializer()),
        LaunchDestination(teamcityapp.features.build_details.api.BuildDetailsReferenceDestination::class, teamcityapp.features.build_details.api.BuildDetailsReferenceDestination.serializer()),
        LaunchDestination(teamcityapp.features.build_details.api.BuildDetailsSectionDestination::class, teamcityapp.features.build_details.api.BuildDetailsSectionDestination.serializer()),
        LaunchDestination(teamcityapp.features.build_history.api.navigation.BuildHistoryDestination::class, teamcityapp.features.build_history.api.navigation.BuildHistoryDestination.serializer()),
        LaunchDestination(teamcityapp.features.build_overview.api.navigation.BuildOverviewDestination::class, teamcityapp.features.build_overview.api.navigation.BuildOverviewDestination.serializer()),
        LaunchDestination(teamcityapp.features.build_queue.api.navigation.BuildQueueDestination::class, teamcityapp.features.build_queue.api.navigation.BuildQueueDestination.serializer()),
        LaunchDestination(teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination::class, teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination.serializer()),
        LaunchDestination(teamcityapp.features.changes.api.navigation.ChangesDestination::class, teamcityapp.features.changes.api.navigation.ChangesDestination.serializer()),
        LaunchDestination(teamcityapp.features.create_account.api.navigation.CreateAccountDestination::class, teamcityapp.features.create_account.api.navigation.CreateAccountDestination.serializer()),
        LaunchDestination(teamcityapp.features.drawer.api.navigation.DrawerDestination::class, teamcityapp.features.drawer.api.navigation.DrawerDestination.serializer()),
        LaunchDestination(teamcityapp.features.favorites.api.navigation.FavoritesDestination::class, teamcityapp.features.favorites.api.navigation.FavoritesDestination.serializer()),
        LaunchDestination(teamcityapp.features.filter_bottom_sheet.api.navigation.QuickFilterDestination::class, teamcityapp.features.filter_bottom_sheet.api.navigation.QuickFilterDestination.serializer()),
        LaunchDestination(teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination::class, teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination.serializer()),
        LaunchDestination(teamcityapp.features.home.api.HomeDestination::class, teamcityapp.features.home.api.HomeDestination.serializer()),
        LaunchDestination(teamcityapp.features.login.api.navigation.LoginDestination::class, teamcityapp.features.login.api.navigation.LoginDestination.serializer()),
        LaunchDestination(teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination::class, teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination.serializer()),
        LaunchDestination(teamcityapp.features.navigation.api.navigation.ProjectDestination::class, teamcityapp.features.navigation.api.navigation.ProjectDestination.serializer()),
        LaunchDestination(teamcityapp.features.properties.api.navigation.PropertiesDestination::class, teamcityapp.features.properties.api.navigation.PropertiesDestination.serializer()),
        LaunchDestination(teamcityapp.features.run_build.api.navigation.RunBuildDestination::class, teamcityapp.features.run_build.api.navigation.RunBuildDestination.serializer()),
        LaunchDestination(teamcityapp.features.running_builds.api.navigation.RunningBuildsDestination::class, teamcityapp.features.running_builds.api.navigation.RunningBuildsDestination.serializer()),
        LaunchDestination(teamcityapp.features.settings.api.navigation.SettingsDestination::class, teamcityapp.features.settings.api.navigation.SettingsDestination.serializer()),
        LaunchDestination(teamcityapp.features.snapshot_dependencies.api.navigation.SnapshotDependenciesDestination::class, teamcityapp.features.snapshot_dependencies.api.navigation.SnapshotDependenciesDestination.serializer()),
        LaunchDestination(teamcityapp.features.splash.api.navigation.SplashNavigationDestination::class, teamcityapp.features.splash.api.navigation.SplashNavigationDestination.serializer()),
        LaunchDestination(teamcityapp.features.test_details.api.navigation.TestDetailsDestination::class, teamcityapp.features.test_details.api.navigation.TestDetailsDestination.serializer()),
        LaunchDestination(teamcityapp.features.tests.api.navigation.TestsDestination::class, teamcityapp.features.tests.api.navigation.TestsDestination.serializer())
    )
)

private class LaunchDestination<T : NavKey>(override val keyType: KClass<T>, private val serializer: KSerializer<T>) : DestinationRegistration {
    override fun registerSerializers(builder: SerializersModuleBuilder) {
        builder.polymorphic(NavKey::class) { subclass(keyType, serializer) }
    }
    override fun entry(visit: NavigationEntry, navigator: Navigator): NavEntry<NavigationEntry> = error("Android launch codecs do not render destinations")
}
