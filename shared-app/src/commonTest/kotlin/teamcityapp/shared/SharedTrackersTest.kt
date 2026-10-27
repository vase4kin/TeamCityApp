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

package teamcityapp.shared

import kotlin.test.*
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.home.api.HomeTab

/** Literal expectations are the pre-migration Firebase contract, not the renamed KMP labels. */
class SharedTrackersTest {
    private val diagnostics = RecordingDiagnostics()
    private fun expect(vararg names: String) = assertEquals(names.map { AnalyticsEvent(it) }, diagnostics.records)
    private fun text(name: String, key: String, value: String) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Text(value)))
    private fun flag(name: String, key: String, value: Boolean) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Flag(value)))

    @Test fun loginPreservesScreenTryItOutAndAuthenticationPayloads() {
        val tracker = SharedLoginTracker(diagnostics)
        tracker.trackView()
        tracker.trackUserClicksOnTryItOut()
        tracker.trackUserTriesTryItOut()
        tracker.trackUserDeclinesTryingTryItOut()
        tracker.trackUserLoginSuccess(true)
        tracker.trackUserLoginSuccess(false)
        tracker.trackGuestUserLoginSuccess(true)
        tracker.trackGuestUserLoginSuccess(false)
        tracker.trackUserLoginFailed("HTTP 401")
        tracker.trackGuestUserLoginFailed("HTTP 500")
        tracker.trackUserDataSaveFailed()
        assertEquals(
            listOf(
                AnalyticsEvent("screen_first_login"), AnalyticsEvent("event_user_clicks_on_try_it_out"),
                AnalyticsEvent("event_user_tries_try_it_out"), AnalyticsEvent("event_user_declines_try_it_out"),
                flag("login_user_success", "sslEnabled", true), flag("login_user_success", "sslEnabled", false),
                flag("login_guest_user_success", "sslEnabled", true), flag("login_guest_user_success", "sslEnabled", false),
                text("login_user_failed", "errorMessage", "HTTP 401"), text("login_guest_user_failed", "errorMessage", "HTTP 500"),
                text("login_user_failed", "errorMessage", "Failed to save user data!")
            ),
            diagnostics.records
        )
    }

    @Test fun addAccountUsesTheSameAuthenticationContractWithItsOwnScreen() {
        val tracker = SharedCreateAccountTracker(diagnostics)
        tracker.trackView()
        tracker.trackUserLoginSuccess(false)
        tracker.trackGuestUserLoginSuccess(true)
        tracker.trackUserLoginFailed("unauthorized")
        tracker.trackGuestUserLoginFailed("")
        tracker.trackUserDataSaveFailed()
        assertEquals(
            listOf(
                AnalyticsEvent("screen_create_account"),
                flag("login_user_success", "sslEnabled", false),
                flag("login_guest_user_success", "sslEnabled", true),
                text("login_user_failed", "errorMessage", "unauthorized"),
                text("login_guest_user_failed", "errorMessage", ""),
                text("login_user_failed", "errorMessage", "Failed to save user data!")
            ),
            diagnostics.records
        )
    }

    @Test fun homePreservesAllFiveLegacyTabNames() {
        val tracker = SharedHomeTracker(diagnostics)
        tracker.viewed()
        HomeTab.entries.forEach(tracker::selected)
        assertEquals(listOf(AnalyticsEvent("screen_home")) + listOf("PROJECTS", "FAVORITES", "RUNNING_BUILDS", "BUILD_QUEUE", "AGENTS").map { text("tab_selected", "tab", it) }, diagnostics.records)
    }

    @Test fun homeRetainsSpecificActionNamesAndProjectsHasNoFabEvent() {
        val tracker = SharedHomeTracker(diagnostics)
        HomeTab.entries.forEach(tracker::fab)
        tracker.addProjects()
        expect("favorites_tab_click_on_fab", "running_builds_tab_click_on_filter_fab", "build_queue_tab_click_on_filter_fab", "agents_tab_click_on_filter_fab", "favorites_tab_click_on_action")
    }

    @Test fun inlineAndSheetFiltersPreserveAllSixLegacyValues() {
        val sheet = SharedQuickFilterTracker(diagnostics)
        QuickFilter.entries.forEach(sheet::selected)
        val expected = listOf(
            text("filter_running_builds_selected", "filter", "RUNNING_ALL"),
            text("filter_running_builds_selected", "filter", "RUNNING_FAVORITES"),
            text("filter_queued_builds_selected", "filter", "QUEUE_ALL"),
            text("filter_queued_builds_selected", "filter", "QUEUE_FAVORITES"),
            text("filter_agents_selected", "filter", "AGENTS_CONNECTED"),
            text("filter_agents_selected", "filter", "AGENTS_DISCONNECTED")
        )
        assertEquals(expected, diagnostics.records)
        diagnostics.records.clear()
        QuickFilter.entries.forEach(SharedHomeTracker(diagnostics)::filterSelected)
        assertEquals(expected, diagnostics.records)
    }

    @Test fun projectsAndRatingPreserveExistingFunnels() {
        val tracker = SharedNavigationTracker(diagnostics)
        tracker.viewShown()
        tracker.ratingShown()
        tracker.ratingCancelled()
        tracker.ratingSelected()
        expect("screen_project", "rate_show", "rate_cancel", "rate_now")
    }

    @Test fun historyPreservesScreenRunAndQueuedDetails() {
        val tracker = SharedHistoryTracker(diagnostics)
        tracker.viewShown()
        tracker.runBuildPressed()
        tracker.queuedBuildRequested()
        expect("screen_build_list", "run_build_fab_click", "build_list_show_queued_details")
    }

    @Test fun managementPreservesEveryRunAndFilterEvent() {
        val run = SharedRunTracker(diagnostics)
        run.trackView()
        run.trackUserRunBuildSuccess()
        run.trackUserRunBuildWithCustomParamsSuccess()
        run.trackUserRunBuildFailed()
        run.trackUserRunBuildFailedForbidden()
        run.trackUserClicksOnAddNewBuildParamButton()
        run.trackUserClicksOnClearAllBuildParamsButton()
        run.trackUserAddsBuildParam()
        val filter = SharedFilterTracker(diagnostics)
        filter.trackView()
        filter.trackUserFilteredBuilds()
        expect("screen_run_build", "run_build_success", "run_build_custom_params_success", "run_build_failed", "run_build_forbidden_error", "run_build_add_custom_param_click", "run_build_clear_all_params", "run_build_add_custom_param", "screen_filter_builds", "build_list_filters_applied")
    }

    @Test fun buildDetailsRetainsQueueFlagsAndDistinctFailureCategories() {
        val tracker = SharedDetailsTracker(diagnostics)
        tracker.viewed()
        tracker.confirmedCancel(true)
        tracker.confirmedCancel(false)
        tracker.cancelSucceeded()
        tracker.cancelFailed(true)
        tracker.cancelFailed(false)
        tracker.restartSucceeded()
        tracker.restartFailed(true)
        tracker.restartFailed(false)
        tracker.queuedOpened()
        tracker.queuedFailed()
        assertEquals(
            listOf(
                AnalyticsEvent("screen_build_details"),
                flag("build_cancel_confirm", "param_re_add_to_the_queue", true),
                flag("build_cancel_confirm", "param_re_add_to_the_queue", false)
            ) + listOf("build_cancel_success", "build_cancel_forbidden_error", "build_cancel_server_error", "build_restart_success", "build_restart_forbidden_error", "build_restart_server_error", "build_restart_show_success", "build_restart_show_failure").map { AnalyticsEvent(it) },
            diagnostics.records
        )
    }

    @Test fun overviewRetainsEveryActionIncludingBranchFilteredHistory() {
        val tracker = SharedOverviewTracker(diagnostics)
        BuildOverviewAction.entries.forEach { tracker.action(it) }
        tracker.action(BuildOverviewAction.Configuration, "private-branch-do-not-log")
        expect("share_build", "open_browser_build", "open_browser_build", "cancel_build", "cancel_build", "restart_build", "open_build_type", "open_project", "show_builds_filtered_by_branch")
    }

    @Test fun detailScreensAndMissingBrowsingActionsRetainTheirEvents() {
        val change = SharedChangeDetailsTracker(diagnostics)
        change.trackView()
        change.trackMoreDetails()
        change.trackFileDiff()
        SharedTestDetailsTracker(diagnostics).trackView()
        SharedBrowsingTracker(diagnostics).apply {
            favoriteConfigurationOpened()
            snapshotDependenciesViewed()
        }
        expect("screen_open_change", "change_click_more_details", "change_click_view_file_diff", "screen_test_details", "favorites_open_build_type", "screen_snapshot_dependencies")
    }

    @Test fun accountActionsRetainDrawerEventIdentities() {
        val tracker = SharedDrawerTracker(diagnostics)
        tracker.trackView()
        tracker.trackChangeAccount()
        tracker.trackOpenPrivacy()
        tracker.trackRateTheApp()
        tracker.trackOpenAbout()
        tracker.trackOpenAddNewAccount()
        tracker.trackOpenManageAccounts()
        tracker.trackOpenSettings()
        expect("screen_open_drawer", "drawer_change_account", "drawer_open_privacy", "drawer_rate_the_app", "drawer_open_about", "drawer_open_add_new_account", "drawer_open_manage_accounts", "drawer_open_settings")
    }

    @Test fun accountManagementAndSettingsRetainTheirExistingNames() {
        val accounts = SharedManageAccountsTracker(diagnostics)
        accounts.trackView()
        accounts.trackAccountRemove()
        accounts.trackUserClicksOnSslDisabledWarning()
        val settings = SharedSettingsTracker(diagnostics)
        settings.trackView()
        settings.trackLightThemeSet()
        settings.trackDarkThemeSet()
        settings.trackAutoBatteryThemeSet()
        settings.trackSystemThemeSet()
        expect("screen_account_list", "remove_account", "account_ssl_warning", "screen_settings", "theme_set_light", "theme_set_dark", "theme_set_auto_battery", "theme_set_system")
    }

    @Test fun queuedResolutionOnlyEmitsTheLegacyRestartFunnelForRestartShowVisits() {
        SharedBuildReferenceTracker(diagnostics, false).apply {
            loaded()
            failed()
        }
        assertTrue(diagnostics.records.isEmpty())
        SharedBuildReferenceTracker(diagnostics, true).apply {
            loaded()
            failed()
        }
        expect("build_restart_show_success", "build_restart_show_failure")
    }

    @Test fun hostsWithoutAnalyticsEmitOnlyFixedLabelsAndDiscardParameters() {
        val labels = mutableListOf<String>()
        val localOnly = object : NativeDiagnostics {
            override fun event(name: String) {
                labels += name
            }
        }
        SharedLoginTracker(localOnly).trackUserLoginFailed("private-message-must-not-enter-OS-logs")
        SharedHomeTracker(localOnly).selected(HomeTab.Projects)
        assertEquals(listOf("login_user_failed", "tab_selected"), labels)
    }

    private class RecordingDiagnostics : NativeDiagnostics {
        val records = mutableListOf<AnalyticsEvent>()
        override fun event(name: String) = error("Expected the structured analytics contract: $name")
        override fun analytics(record: AnalyticsEvent) {
            records += record
        }
    }
}
