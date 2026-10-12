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

import android.content.Intent
import androidx.core.content.IntentCompat
import androidx.navigation3.runtime.NavKey
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import java.io.Serializable
import javax.inject.Inject
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.build_details.api.BuildDetailsDestination
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.home.api.HomeTab
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.run_build.api.navigation.RunBuildDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.features.splash.api.navigation.AccountActivationDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.navigation.DestinationRegistry

/** The app owns installed component compatibility; feature APIs retain only destination keys. */
class IncomingDestinationResolver @Inject constructor(private val registry: DestinationRegistry, private val codec: BuildLaunchCodec, private val historyFilters: BuildHistoryFilterAdapter) {
    fun destination(intent: Intent): NavKey? {
        intent.getStringExtra(MainActivity.EXTRA_DESTINATION)?.let { return registry.decodeDestination(it) }
        return when (intent.component?.className) {
            BUILD_DETAILS_COMPONENT -> {
                val payload = requireNotNull(IntentCompat.getSerializableExtra(intent, BundleExtractorValues.BUILD, Serializable::class.java))
                BuildDetailsDestination(codec.decode(payload), intent.getStringExtra(BundleExtractorValues.NAME))
            }

            HOME_COMPONENT -> HomeDestination(HomeTab.entries.getOrElse(intent.getIntExtra(HOME_TAB, -1)) { HomeTab.Projects })

            RUN_BUILD_COMPONENT, LEGACY_RUN_BUILD_COMPONENT -> RunBuildDestination(intent.getStringExtra(RUN_BUILD_TYPE_ID).orEmpty())

            FILTER_BUILDS_COMPONENT, LEGACY_FILTER_BUILDS_COMPONENT -> FilterBuildsDestination(intent.getStringExtra(RUN_BUILD_TYPE_ID).orEmpty())

            ABOUT_COMPONENT -> AboutDestination

            SETTINGS_COMPONENT -> SettingsDestination

            SPLASH_COMPONENT -> SplashNavigationDestination

            TEST_DETAILS_COMPONENT -> TestDetailsDestination(intent.getStringExtra("arg_test_url").orEmpty())

            CHANGE_DETAILS_COMPONENT -> ChangeDetailsDestination(changeDetails(intent))

            MANAGE_ACCOUNTS_COMPONENT -> ManageAccountsDestination

            LOGIN_COMPONENT, LEGACY_LOGIN_COMPONENT -> LoginDestination

            CREATE_ACCOUNT_COMPONENT, LEGACY_CREATE_ACCOUNT_COMPONENT -> CreateAccountDestination

            PROJECT_COMPONENT, LegacyProjectEntry.LEGACY_ACTIVITY -> ProjectDestination(intent.getStringExtra(LegacyProjectEntry.PROJECT_ID) ?: LegacyProjectEntry.ROOT_PROJECT_ID, intent.getStringExtra(LegacyProjectEntry.PROJECT_NAME).orEmpty(), false)

            HISTORY_COMPONENT, LegacyBuildHistoryEntry.LEGACY_ACTIVITY -> BuildHistoryDestination(intent.getStringExtra(LegacyBuildHistoryEntry.EXTRA_ID).orEmpty(), intent.getStringExtra(LegacyBuildHistoryEntry.EXTRA_NAME).orEmpty(), historyLocator(intent))

            ARTIFACTS_COMPONENT, LegacyArtifactsEntry.LEGACY_ACTIVITY -> {
                val payload = requireNotNull(IntentCompat.getSerializableExtra(intent, LegacyArtifactsEntry.BUILD, Serializable::class.java))
                ArtifactsDestination(codec.decode(payload), intent.getStringExtra(LegacyArtifactsEntry.URL).orEmpty(), intent.getStringExtra(LegacyArtifactsEntry.NAME).orEmpty())
            }

            else -> null
        }
    }
    fun normalizedIntent(intent: Intent, destination: NavKey) = Intent(intent).apply {
        putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(destination))
        // Only plain destination arguments enter subsequent recreation; legacy DTO names
        // still belong to their compatibility codec/cache boundary.
        if (destination is BuildHistoryDestination) removeExtra(LegacyBuildHistoryEntry.EXTRA_LEGACY_FILTER)
        if (destination is ArtifactsDestination) removeExtra(LegacyArtifactsEntry.BUILD)
        if (destination is BuildDetailsDestination) removeExtra(BundleExtractorValues.BUILD)
        if (destination is HomeDestination || destination is AccountActivationDestination) {
            removeExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD)
            removeExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED)
        }
    }

    fun requiresHomeReset(intent: Intent) = intent.getBooleanExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, false) || intent.getBooleanExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, false)

    private fun historyLocator(intent: Intent): String? {
        if (intent.hasExtra(LegacyBuildHistoryEntry.EXTRA_LOCATOR)) return intent.getStringExtra(LegacyBuildHistoryEntry.EXTRA_LOCATOR)
        return IntentCompat.getSerializableExtra(intent, LegacyBuildHistoryEntry.EXTRA_LEGACY_FILTER, Serializable::class.java)?.let(historyFilters::locator)
    }

    private fun changeDetails(intent: Intent): ChangeDetails? {
        val id = intent.getStringExtra("change:id") ?: return null
        val names = intent.getStringArrayListExtra("change:file_names") ?: return null
        val types = intent.getStringArrayListExtra("change:file_types") ?: return null
        if (names.size != types.size) return null
        fun text(key: String) = intent.getStringExtra(key).orEmpty()
        return ChangeDetails(id, text("change:comment"), text("change:user"), text("change:date"), names.indices.map { ChangedFile(names[it], types[it]) }, text("change:revision"), text("change:web_url"))
    }

    companion object {
        const val RUN_BUILD_TYPE_ID = "BuildTypeId"
        const val BUILD_DETAILS_COMPONENT = "com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity"
        const val HOME_COMPONENT = "com.github.vase4kin.teamcityapp.home.view.HomeActivity"
        const val HOME_TAB = "arg_tab"
        const val RUN_BUILD_COMPONENT = "teamcityapp.features.run_build.impl.RunBuildActivity"
        const val LEGACY_RUN_BUILD_COMPONENT = "com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity"
        const val FILTER_BUILDS_COMPONENT = "teamcityapp.features.filter_builds.impl.FilterBuildsActivity"
        const val LEGACY_FILTER_BUILDS_COMPONENT = "com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity"

        const val PROJECT_COMPONENT = "teamcityapp.features.navigation.impl.NavigationActivity"
        const val HISTORY_COMPONENT = "teamcityapp.features.build_history.impl.BuildHistoryActivity"
        const val ARTIFACTS_COMPONENT = "teamcityapp.features.artifacts.impl.ArtifactsActivity"
        const val LOGIN_COMPONENT = "teamcityapp.features.login.impl.LoginActivity"
        const val LEGACY_LOGIN_COMPONENT = "com.github.vase4kin.teamcityapp.login.view.LoginActivity"
        const val CREATE_ACCOUNT_COMPONENT = "teamcityapp.features.create_account.impl.CreateAccountActivity"
        const val LEGACY_CREATE_ACCOUNT_COMPONENT = "com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity"
        const val TEST_DETAILS_COMPONENT = "teamcityapp.features.test_details.impl.TestDetailsActivity"
        const val CHANGE_DETAILS_COMPONENT = "teamcityapp.features.change_details.impl.ChangeDetailsActivity"
        const val MANAGE_ACCOUNTS_COMPONENT = "teamcityapp.features.manage_accounts.impl.ManageAccountsActivity"
        const val SPLASH_COMPONENT = "teamcityapp.features.splash.impl.SplashActivity"
        const val ABOUT_COMPONENT = "teamcityapp.features.about.impl.AboutActivity"
        const val SETTINGS_COMPONENT = "teamcityapp.features.settings.impl.SettingsActivity"
    }
}
