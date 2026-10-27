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

import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTracker
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.change_details.impl.tracker.ChangeDetailsTracker
import teamcityapp.features.create_account.api.CreateAccountTracker
import teamcityapp.features.drawer.impl.tracker.DrawerTracker
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterTracker
import teamcityapp.features.filter_builds.api.FilterBuildsTracker
import teamcityapp.features.home.api.HomeTab
import teamcityapp.features.home.impl.tracker.HomeTracker
import teamcityapp.features.login.api.LoginTracker
import teamcityapp.features.manage_accounts.impl.tracker.ManageAccountsTracker
import teamcityapp.features.navigation.impl.tracker.NavigationTracker
import teamcityapp.features.run_build.api.RunBuildTracker
import teamcityapp.features.settings.impl.tracker.SettingsTracker
import teamcityapp.features.test_details.impl.tracker.TestDetailsTracker

/** Keep legacy Firebase event identities here; platform adapters own SDKs and OS diagnostics. */
internal class SharedAuthenticationTracker(private val diagnostics: NativeDiagnostics) {
    fun userSuccess(ssl: Boolean) = diagnostics.track("login_user_success", "sslEnabled" to AnalyticsValue.Flag(ssl))
    fun guestSuccess(ssl: Boolean) = diagnostics.track("login_guest_user_success", "sslEnabled" to AnalyticsValue.Flag(ssl))
    fun userFailure(message: String) = diagnostics.track("login_user_failed", "errorMessage" to AnalyticsValue.Text(message))
    fun guestFailure(message: String) = diagnostics.track("login_guest_user_failed", "errorMessage" to AnalyticsValue.Text(message))
    fun saveFailure() = userFailure(CreateAccountTracker.MESSAGE_ERROR_SAVE_DATE)
}
internal class SharedLoginTracker(private val diagnostics: NativeDiagnostics) : LoginTracker {
    private val authentication = SharedAuthenticationTracker(diagnostics)
    override fun trackView() = diagnostics.track(LoginTracker.SCREEN_NAME)
    override fun trackUserLoginSuccess(isSslEnabled: Boolean) = authentication.userSuccess(isSslEnabled)
    override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) = authentication.guestSuccess(isSslEnabled)
    override fun trackUserLoginFailed(errorMessage: String) = authentication.userFailure(errorMessage)
    override fun trackGuestUserLoginFailed(errorMessage: String) = authentication.guestFailure(errorMessage)
    override fun trackUserDataSaveFailed() = authentication.saveFailure()
    override fun trackUserClicksOnTryItOut() = diagnostics.track(LoginTracker.EVENT_USER_CLICKS_ON_TRY_IT_OUT)
    override fun trackUserTriesTryItOut() = diagnostics.track(LoginTracker.EVENT_USER_TRIES_TRY_IT_OUT)
    override fun trackUserDeclinesTryingTryItOut() = diagnostics.track(LoginTracker.EVENT_USER_DECLINES_TRY_IT_OUT)
}
internal class SharedCreateAccountTracker(private val diagnostics: NativeDiagnostics) : CreateAccountTracker {
    private val authentication = SharedAuthenticationTracker(diagnostics)
    override fun trackView() = diagnostics.track(CreateAccountTracker.SCREEN_NAME)
    override fun trackUserLoginSuccess(isSslEnabled: Boolean) = authentication.userSuccess(isSslEnabled)
    override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) = authentication.guestSuccess(isSslEnabled)
    override fun trackUserLoginFailed(errorMessage: String) = authentication.userFailure(errorMessage)
    override fun trackGuestUserLoginFailed(errorMessage: String) = authentication.guestFailure(errorMessage)
    override fun trackUserDataSaveFailed() = authentication.saveFailure()
}
internal class SharedNavigationTracker(private val diagnostics: NativeDiagnostics) : NavigationTracker {
    override fun viewShown() = diagnostics.track("screen_project")
    override fun ratingShown() = diagnostics.track("rate_show")
    override fun ratingCancelled() = diagnostics.track("rate_cancel")
    override fun ratingSelected() = diagnostics.track("rate_now")
}
internal class SharedHistoryTracker(private val diagnostics: NativeDiagnostics) : BuildHistoryTracker {
    override fun viewShown() = diagnostics.track("screen_build_list")
    override fun runBuildPressed() = diagnostics.track("run_build_fab_click")
    override fun queuedBuildRequested() = diagnostics.track("build_list_show_queued_details")
}
internal class SharedHomeTracker(private val diagnostics: NativeDiagnostics) : HomeTracker {
    override fun viewed() = diagnostics.track("screen_home")
    override fun selected(tab: HomeTab) = diagnostics.track(
        "tab_selected",
        "tab" to AnalyticsValue.Text(
            when (tab) {
                HomeTab.Projects -> "PROJECTS"
                HomeTab.Favorites -> "FAVORITES"
                HomeTab.Running -> "RUNNING_BUILDS"
                HomeTab.Queue -> "BUILD_QUEUE"
                HomeTab.Agents -> "AGENTS"
            }
        )
    )
    override fun fab(tab: HomeTab) {
        val event = when (tab) {
            HomeTab.Favorites -> "favorites_tab_click_on_fab"
            HomeTab.Running -> "running_builds_tab_click_on_filter_fab"
            HomeTab.Queue -> "build_queue_tab_click_on_filter_fab"
            HomeTab.Agents -> "agents_tab_click_on_filter_fab"
            HomeTab.Projects -> return
        }
        diagnostics.track(event)
    }
    override fun addProjects() = diagnostics.track("favorites_tab_click_on_action")
    override fun filterSelected(filter: QuickFilter) = SharedQuickFilterTracker(diagnostics).selected(filter)
}
internal class SharedQuickFilterTracker(private val diagnostics: NativeDiagnostics) : QuickFilterTracker {
    override fun selected(filter: QuickFilter) {
        val (event, value) = when (filter) {
            QuickFilter.RunningAll -> "filter_running_builds_selected" to "RUNNING_ALL"
            QuickFilter.RunningFavorites -> "filter_running_builds_selected" to "RUNNING_FAVORITES"
            QuickFilter.QueuedAll -> "filter_queued_builds_selected" to "QUEUE_ALL"
            QuickFilter.QueuedFavorites -> "filter_queued_builds_selected" to "QUEUE_FAVORITES"
            QuickFilter.AgentsConnected -> "filter_agents_selected" to "AGENTS_CONNECTED"
            QuickFilter.AgentsDisconnected -> "filter_agents_selected" to "AGENTS_DISCONNECTED"
        }
        diagnostics.track(event, "filter" to AnalyticsValue.Text(value))
    }
}
internal class SharedChangeDetailsTracker(private val diagnostics: NativeDiagnostics) : ChangeDetailsTracker {
    override fun trackView() = diagnostics.track("screen_open_change")
    override fun trackMoreDetails() = diagnostics.track("change_click_more_details")
    override fun trackFileDiff() = diagnostics.track("change_click_view_file_diff")
}
internal class SharedTestDetailsTracker(private val diagnostics: NativeDiagnostics) : TestDetailsTracker {
    override fun trackView() = diagnostics.track("screen_test_details")
}
internal class SharedRunTracker(private val diagnostics: NativeDiagnostics) : RunBuildTracker {
    override fun trackView() = diagnostics.track(RunBuildTracker.SCREEN_NAME_RUN_BUILD)
    override fun trackUserRunBuildSuccess() = diagnostics.track(RunBuildTracker.EVENT_RUN_BUILD_SUCCESS)
    override fun trackUserRunBuildWithCustomParamsSuccess() = diagnostics.track(RunBuildTracker.EVENT_RUN_BUILD_SUCCESS_WITH_CUSTOM_PARAMETERS)
    override fun trackUserRunBuildFailed() = diagnostics.track(RunBuildTracker.EVENT_RUN_BUILD_FAILED)
    override fun trackUserRunBuildFailedForbidden() = diagnostics.track(RunBuildTracker.EVENT_RUN_BUILD_FAILED_FORBIDDEN)
    override fun trackUserClicksOnAddNewBuildParamButton() = diagnostics.track(RunBuildTracker.EVENT_USER_CLICKS_ADD_NEW_BUILD_PARAMETER_BUTTON)
    override fun trackUserClicksOnClearAllBuildParamsButton() = diagnostics.track(RunBuildTracker.EVENT_USER_CLICKS_CLEAR_ALL_BUILD_PARAMETERS_BUTTON)
    override fun trackUserAddsBuildParam() = diagnostics.track(RunBuildTracker.EVENT_USER_ADDS_NEW_BUILD_PARAMETER)
}
internal class SharedFilterTracker(private val diagnostics: NativeDiagnostics) : FilterBuildsTracker {
    override fun trackView() = diagnostics.track(FilterBuildsTracker.SCREEN_NAME_FILTER_BUILDS)
    override fun trackUserFilteredBuilds() = diagnostics.track(FilterBuildsTracker.EVENT_RUN_BUILD_BUTTON_PRESSED)
}
internal class SharedDetailsTracker(private val diagnostics: NativeDiagnostics) : BuildDetailsTracker {
    override fun viewed() = diagnostics.track("screen_build_details")
    override fun confirmedCancel(reAdd: Boolean) = diagnostics.track("build_cancel_confirm", "param_re_add_to_the_queue" to AnalyticsValue.Flag(reAdd))
    override fun cancelSucceeded() = diagnostics.track("build_cancel_success")
    override fun cancelFailed(forbidden: Boolean) = diagnostics.track(if (forbidden) "build_cancel_forbidden_error" else "build_cancel_server_error")
    override fun restartSucceeded() = diagnostics.track("build_restart_success")
    override fun restartFailed(forbidden: Boolean) = diagnostics.track(if (forbidden) "build_restart_forbidden_error" else "build_restart_server_error")
    override fun queuedOpened() = diagnostics.track("build_restart_show_success")
    override fun queuedFailed() = diagnostics.track("build_restart_show_failure")
}
internal class SharedOverviewTracker(private val diagnostics: NativeDiagnostics) {
    fun action(action: BuildOverviewAction, branch: String? = null) = diagnostics.track(
        when (action) {
            BuildOverviewAction.Share -> "share_build"
            BuildOverviewAction.Browser, BuildOverviewAction.Log -> "open_browser_build"
            BuildOverviewAction.Stop, BuildOverviewAction.RemoveFromQueue -> "cancel_build"
            BuildOverviewAction.Restart -> "restart_build"
            BuildOverviewAction.Configuration -> if (branch != null) "show_builds_filtered_by_branch" else "open_build_type"
            BuildOverviewAction.Project -> "open_project"
        }
    )
}
internal class SharedBrowsingTracker(private val diagnostics: NativeDiagnostics) {
    fun favoriteConfigurationOpened() = diagnostics.track("favorites_open_build_type")
    fun snapshotDependenciesViewed() = diagnostics.track("screen_snapshot_dependencies")
}
internal class SharedManageAccountsTracker(private val diagnostics: NativeDiagnostics) : ManageAccountsTracker {
    override fun trackView() = diagnostics.track(ManageAccountsTracker.SCREEN_NAME)
    override fun trackAccountRemove() = diagnostics.track(ManageAccountsTracker.EVENT_REMOVE_ACCOUNT)
    override fun trackUserClicksOnSslDisabledWarning() = diagnostics.track(ManageAccountsTracker.EVENT_SSL_WARNING)
}
internal class SharedDrawerTracker(private val diagnostics: NativeDiagnostics) : DrawerTracker {
    override fun trackChangeAccount() = diagnostics.track(DrawerTracker.EVENT_CHANGE_ACCOUNT)
    override fun trackOpenPrivacy() = diagnostics.track(DrawerTracker.EVENT_OPEN_PRIVACY)
    override fun trackRateTheApp() = diagnostics.track(DrawerTracker.EVENT_RATE_THE_APP)
    override fun trackOpenAbout() = diagnostics.track(DrawerTracker.EVENT_OPEN_ABOUT)
    override fun trackOpenAddNewAccount() = diagnostics.track(DrawerTracker.EVENT_OPEN_ADD_NEW_ACCOUNT)
    override fun trackOpenManageAccounts() = diagnostics.track(DrawerTracker.EVENT_OPEN_MANAGE_ACCOUNTS)
    override fun trackOpenSettings() = diagnostics.track(DrawerTracker.EVENT_OPEN_SETTINGS)
    override fun trackView() = diagnostics.track(DrawerTracker.SCREEN_NAME)
}
internal class SharedSettingsTracker(private val diagnostics: NativeDiagnostics) : SettingsTracker {
    override fun trackView() = diagnostics.track(SettingsTracker.SCREEN_NAME)
    override fun trackLightThemeSet() = diagnostics.track(SettingsTracker.EVENT_NAME_LIGHT_THEME)
    override fun trackDarkThemeSet() = diagnostics.track(SettingsTracker.EVENT_NAME_DARK_THEME)
    override fun trackAutoBatteryThemeSet() = diagnostics.track(SettingsTracker.EVENT_NAME_AUTO_BATTERY_THEME)
    override fun trackSystemThemeSet() = diagnostics.track(SettingsTracker.EVENT_NAME_SYSTEM_THEME)
}

/** Only restart Show visits owned this funnel in the installed Android app. */
internal class SharedBuildReferenceTracker(diagnostics: NativeDiagnostics, private val fromRestart: Boolean) : teamcityapp.features.build_overview.impl.BuildReferenceTracker {
    private val details = SharedDetailsTracker(diagnostics)
    override fun loaded() {
        if (fromRestart) details.queuedOpened()
    }
    override fun failed() {
        if (fromRestart) details.queuedFailed()
    }
}
