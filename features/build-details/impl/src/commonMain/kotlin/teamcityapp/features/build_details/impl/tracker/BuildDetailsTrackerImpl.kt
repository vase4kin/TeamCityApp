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

package teamcityapp.features.build_details.impl.tracker

import teamcityapp.libraries.analytics.*

class BuildDetailsTrackerImpl(private val analytics: AnalyticsTracker) : BuildDetailsTracker {
    override fun viewed() = analytics.track("screen_build_details")
    override fun confirmedCancel(reAdd: Boolean) = analytics.track("build_cancel_confirm", "param_re_add_to_the_queue" to AnalyticsValue.Flag(reAdd))
    override fun cancelSucceeded() = analytics.track("build_cancel_success")
    override fun cancelFailed(forbidden: Boolean) = analytics.track(if (forbidden) "build_cancel_forbidden_error" else "build_cancel_server_error")
    override fun restartSucceeded() = analytics.track("build_restart_success")
    override fun restartFailed(forbidden: Boolean) = analytics.track(if (forbidden) "build_restart_forbidden_error" else "build_restart_server_error")
    override fun queuedOpened() = analytics.track("build_restart_show_success")
    override fun queuedFailed() = analytics.track("build_restart_show_failure")
}
