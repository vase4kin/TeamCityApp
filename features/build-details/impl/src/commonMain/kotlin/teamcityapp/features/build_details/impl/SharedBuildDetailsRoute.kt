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

package teamcityapp.features.build_details.impl

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.build_details.api.BuildDetailsCommand
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.builds.BuildLaunchData

/** Build management presentation owns confirmations, education, notices and loaded-build actions. */
@Composable
fun SharedBuildDetailsRoute(
    model: BuildDetailsViewModel,
    education: BuildDetailsMetadataViewModel,
    loadedBuild: BuildLaunchData?,
    onBuildViewed: suspend (BuildLaunchData) -> Unit,
    onRefreshOverview: () -> Unit,
    onAction: (BuildOverviewAction, BuildLaunchData) -> Unit,
    onBack: () -> Unit,
    onQueuedBuild: (String, String) -> Unit,
    onReplacement: (BuildDetailsReplacement, String) -> Boolean,
    content: @Composable (busy: Boolean, current: BuildLaunchData, subtitle: String) -> Unit
) {
    LifecycleResumeEffect(model) {
        model.viewed()
        onPauseOrDispose {}
    }
    val state by model.state.collectAsStateWithLifecycle()
    val educationState by education.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.current) { education.loaded(state.current) }
    LaunchedEffect(loadedBuild) {
        loadedBuild?.takeIf { it.id == state.current.id }?.let { build ->
            model.loaded(build)
            // Optional rating bookkeeping cannot turn a loaded build into a UI failure.
            try {
                onBuildViewed(build)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) { /* A later view can retry this preference write. */ }
        }
    }
    LaunchedEffect(state.overviewRefresh) { if (state.overviewRefresh > 0) onRefreshOverview() }
    val snackbar = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val message = state.notice?.let { stringResource(it.message) }
    val action = state.notice?.actionLabel?.let { stringResource(it) }
    LaunchedEffect(state.notice?.id, lifecycle) {
        val notice = state.notice ?: return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val result = snackbar.showSnackbar(checkNotNull(message), action, duration = SnackbarDuration.Long)
            model.consumeNotice(notice.id)
            if (result == SnackbarResult.ActionPerformed && notice.openQueued) model.queuedBuildReference()?.let { onQueuedBuild(it, state.subtitle) }
        }
    }
    LifecycleResumeEffect(state.replacement, model) {
        state.replacement?.let { if (onReplacement(it, state.subtitle)) model.consumeReplacement(it.id) }
        onPauseOrDispose {}
    }
    BuildDetailsScreen(state.copy(prompt = educationState.prompt, onboardingFailed = educationState.onboardingFailed), onBack, { command ->
        performBuildDetailsCommand(command, state.current, onAction, model::requestCancellation, model::requestRestart)
    }, model::confirm, model::dismissConfirmation, snackbar, onDismissPrompt = education::dismissPrompt) {
        content(state.busy, state.current, state.subtitle)
    }
}

internal fun performBuildDetailsCommand(command: BuildDetailsCommand, build: BuildLaunchData, onAction: (BuildOverviewAction, BuildLaunchData) -> Unit, onCancel: () -> Unit, onRestart: () -> Unit) {
    when (command) {
        BuildDetailsCommand.Share -> onAction(BuildOverviewAction.Share, build)

        BuildDetailsCommand.Browser -> onAction(BuildOverviewAction.Browser, build)

        BuildDetailsCommand.Log -> onAction(BuildOverviewAction.Log, build)

        BuildDetailsCommand.Cancel -> {
            onAction(if (build.state == "queued") BuildOverviewAction.RemoveFromQueue else BuildOverviewAction.Stop, build)
            onCancel()
        }

        BuildDetailsCommand.Restart -> {
            onAction(BuildOverviewAction.Restart, build)
            onRestart()
        }

        else -> Unit
    }
}
