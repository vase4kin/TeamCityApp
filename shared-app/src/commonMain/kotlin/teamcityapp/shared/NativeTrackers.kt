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

import teamcityapp.features.build_history.impl.tracker.BuildHistoryTracker
import teamcityapp.features.login.api.LoginTracker
import teamcityapp.features.navigation.impl.tracker.NavigationTracker

/** Shared event decisions; native adapters choose the OS diagnostics backend. */
internal class NativeLoginTracker(private val diagnostics: NativeDiagnostics) : LoginTracker {
    override fun trackView() = diagnostics.event("login_view")
    override fun trackUserLoginSuccess(isSslEnabled: Boolean) = diagnostics.event("login_user_login_success")
    override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) = diagnostics.event("login_guest_user_login_success")
    override fun trackUserLoginFailed(errorMessage: String) = diagnostics.event("login_user_login_failed")
    override fun trackGuestUserLoginFailed(errorMessage: String) = diagnostics.event("login_guest_user_login_failed")
    override fun trackUserDataSaveFailed() = diagnostics.event("login_user_data_save_failed")
    override fun trackUserClicksOnTryItOut() = diagnostics.event("login_user_clicks_on_try_it_out")
    override fun trackUserTriesTryItOut() = diagnostics.event("login_user_tries_try_it_out")
    override fun trackUserDeclinesTryingTryItOut() = diagnostics.event("login_user_declines_trying_try_it_out")
}
internal class NativeNavigationTracker(private val diagnostics: NativeDiagnostics) : NavigationTracker {
    override fun viewShown() = diagnostics.event("projects_view_shown")
    override fun ratingShown() = diagnostics.event("projects_rating_shown")
    override fun ratingCancelled() = diagnostics.event("projects_rating_cancelled")
    override fun ratingSelected() = diagnostics.event("projects_rating_selected")
}
internal class NativeHistoryTracker(private val diagnostics: NativeDiagnostics) : BuildHistoryTracker {
    override fun viewShown() = diagnostics.event("history_view_shown")
    override fun runBuildPressed() = diagnostics.event("history_run_build_pressed")
    override fun queuedBuildRequested() = diagnostics.event("history_queued_build_requested")
}
