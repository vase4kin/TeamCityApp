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

/** iOS analytics will be connected with the platform integrations phase. */
internal object NativeLoginTracker : LoginTracker {
    override fun trackView() {}
    override fun trackUserLoginSuccess(isSslEnabled: Boolean) {}
    override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) {}
    override fun trackUserLoginFailed(errorMessage: String) {}
    override fun trackGuestUserLoginFailed(errorMessage: String) {}
    override fun trackUserDataSaveFailed() {}
    override fun trackUserClicksOnTryItOut() {}
    override fun trackUserTriesTryItOut() {}
    override fun trackUserDeclinesTryingTryItOut() {}
}
internal object NativeNavigationTracker : NavigationTracker {
    override fun viewShown() {}
    override fun ratingShown() {}
    override fun ratingCancelled() {}
    override fun ratingSelected() {}
}
internal object NativeHistoryTracker : BuildHistoryTracker {
    override fun viewShown() {}
    override fun runBuildPressed() {}
    override fun queuedBuildRequested() {}
}
