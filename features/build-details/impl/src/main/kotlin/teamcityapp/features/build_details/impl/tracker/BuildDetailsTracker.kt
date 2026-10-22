/*
 * Copyright 2019 Andrey Tolpeev
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

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

internal class FirebaseBuildDetailsTracker @Inject constructor(private val analytics: FirebaseAnalytics) : BuildDetailsTracker {
    override fun viewed() = analytics.logEvent("screen_build_details", null)
    override fun confirmedCancel(reAdd: Boolean) = analytics.logEvent("build_cancel_confirm", Bundle().apply { putBoolean("param_re_add_to_the_queue", reAdd) })
    override fun cancelSucceeded() = analytics.logEvent("build_cancel_success", null)
    override fun cancelFailed(forbidden: Boolean) = analytics.logEvent(if (forbidden) "build_cancel_forbidden_error" else "build_cancel_server_error", null)
    override fun restartSucceeded() = analytics.logEvent("build_restart_success", null)
    override fun restartFailed(forbidden: Boolean) = analytics.logEvent(if (forbidden) "build_restart_forbidden_error" else "build_restart_server_error", null)
    override fun queuedOpened() = analytics.logEvent("build_restart_show_success", null)
    override fun queuedFailed() = analytics.logEvent("build_restart_show_failure", null)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BuildDetailsModule {
    @Binds abstract fun tracker(implementation: FirebaseBuildDetailsTracker): BuildDetailsTracker
}
