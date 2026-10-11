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

package com.github.vase4kin.teamcityapp.overview.router

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsArguments
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.overview.data.*
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.api.BuildOverviewAppRouter
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.onboarding.OnboardingManager

/** Native confirmation/share/navigation listeners observe the loaded model before synchronous EventBus dispatch. */
@ActivityScoped
class AppBuildOverviewRouterImpl internal constructor(
    private val owner: Activity,
    private val arguments: BuildDetailsArguments,
    private val mapper: AppBuildLaunchMapper,
    private val eventBus: EventBus,
    private val prompts: BuildOverviewPrompts,
    private val handler: Handler = Handler(Looper.getMainLooper())
) : BuildOverviewAppRouter {
    @Inject constructor(
        activity: Activity,
        arguments: BuildDetailsArguments,
        mapper: AppBuildLaunchMapper,
        eventBus: EventBus,
        onboarding: OnboardingManager
    ) : this(activity, arguments, mapper, eventBus, ToolbarBuildOverviewPrompts(activity, onboarding))

    private var sheetVisible = false
    private var restoreFab: Runnable? = null

    override fun buildLoaded(activity: Activity, build: BuildLaunchData) {
        requireOwner(activity)
        arguments.publishLoaded(mapper.toLegacyBuild(build))
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "#${arguments.current.number}"
            subtitle = arguments.current.buildTypeName
        }
    }
    override fun dispatch(activity: Activity, action: BuildOverviewAction, build: BuildLaunchData, branch: String?) {
        buildLoaded(activity, build)
        val event: Any = when (action) {
            BuildOverviewAction.Share -> ShareBuildEvent()
            BuildOverviewAction.Browser -> OpenBrowserEvent()
            BuildOverviewAction.Stop, BuildOverviewAction.RemoveFromQueue -> StopBuildEvent()
            BuildOverviewAction.Restart -> RestartBuildEvent()
            BuildOverviewAction.Configuration -> if (branch == null) StartBuildsListActivityEvent() else StartBuildsListActivityFilteredByBranchEvent(branch)
            BuildOverviewAction.Project -> StartProjectActivityEvent()
        }
        eventBus.post(event)
    }
    override fun resumed(activity: Activity, build: BuildLaunchData) {
        requireOwner(activity)
        buildLoaded(activity, build)
        prompts.resumed(build)
    }
    override fun sheetVisibility(activity: Activity, visible: Boolean) {
        requireOwner(activity)
        if (visible) {
            restoreFab?.let(handler::removeCallbacks)
            restoreFab = null
            sheetVisible = true
            eventBus.post(FloatButtonChangeVisibilityEvent(View.GONE))
        } else if (sheetVisible) {
            sheetVisible = false
            restoreFab?.let(handler::removeCallbacks)
            restoreFab = Runnable {
                restoreFab = null
                eventBus.post(FloatButtonChangeVisibilityEvent(View.VISIBLE))
            }.also { handler.postDelayed(it, 500) }
        }
    }
    override fun dispose(activity: Activity) {
        requireOwner(activity)
        prompts.dispose()
        val mustRestore = sheetVisible || restoreFab != null
        restoreFab?.let(handler::removeCallbacks)
        restoreFab = null
        sheetVisible = false
        if (mustRestore) eventBus.post(FloatButtonChangeVisibilityEvent(View.VISIBLE))
    }
    private fun requireOwner(activity: Activity) = require(activity === owner) { "Overview adapter must use its owning BuildDetails activity" }
}
