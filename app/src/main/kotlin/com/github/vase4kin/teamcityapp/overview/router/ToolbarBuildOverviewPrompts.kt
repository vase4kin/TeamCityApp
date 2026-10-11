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
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.github.vase4kin.teamcityapp.R
import com.google.android.material.elevation.ElevationOverlayProvider
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.utils.getThemeColor
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

internal interface BuildOverviewPrompts {
    fun resumed(build: BuildLaunchData)
    fun dispose()
}

/** Keeps shared prompt preferences and the native overflow target until BuildDetails owns Compose navigation. */
internal class ToolbarBuildOverviewPrompts(
    private val activity: Activity,
    private val onboarding: OnboardingManager,
    private val handler: Handler = Handler(Looper.getMainLooper())
) : BuildOverviewPrompts {
    private var pending: Runnable? = null
    private var prompt: MaterialTapTargetPrompt? = null

    override fun resumed(build: BuildLaunchData) {
        dispose()
        val secondary: Int
        val handled: () -> Unit
        when {
            build.isFinished && !onboarding.isRestartBuildPromptShown -> {
                secondary = R.string.text_onboarding_restart_build
                handled = onboarding::saveRestartBuildPromptShown
            }

            build.isRunning && !onboarding.isStopBuildPromptShown -> {
                secondary = R.string.text_onboarding_stop_build
                handled = onboarding::saveStopBuildPromptShown
            }

            build.isQueued && !onboarding.isRemoveBuildFromQueuePromptShown -> {
                secondary = R.string.text_onboarding_remove_build_from_queue
                handled = onboarding::saveRemoveBuildFromQueuePromptShown
            }

            else -> return
        }
        pending = Runnable {
            pending = null
            if (activity.isFinishing || activity.isDestroyed) return@Runnable
            val toolbar = activity.findViewById<Toolbar>(R.id.toolbar) ?: return@Runnable
            val menu = (0 until toolbar.childCount).map { toolbar.getChildAt(it) }.filterIsInstance<ActionMenuView>().lastOrNull() ?: return@Runnable
            val target = menu.getChildAt(menu.childCount - 1) ?: return@Runnable
            val elevation = activity.resources.getDimension(R.dimen.dp_4)
            val color = ElevationOverlayProvider(activity).compositeOverlayIfNeeded(activity.getThemeColor(R.attr.colorPrimarySurface), elevation)
            prompt = MaterialTapTargetPrompt.Builder(activity)
                .setPrimaryText(R.string.title_onboarding_build_menu)
                .setSecondaryText(secondary)
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_more_vert_black_24dp)
                .setIconDrawableTintList(ColorStateList.valueOf(color))
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener { _, _ -> handled() }
                .setTarget(target)
                .show()
        }.also { handler.postDelayed(it, 500) }
    }
    override fun dispose() {
        pending?.let(handler::removeCallbacks)
        pending = null
        prompt?.dismiss()
        prompt = null
    }
}
