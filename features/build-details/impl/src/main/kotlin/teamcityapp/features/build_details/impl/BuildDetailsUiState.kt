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

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData

internal typealias BuildDetailsAction = BuildDetailsCommand
internal enum class BuildDetailsOperation(@StringRes val message: Int) {
    Stop(R.string.loading_stop),
    Remove(R.string.loading_remove),
    Restart(R.string.loading_restart)
}

@Serializable
internal enum class ConfirmationKind { Stop, Remove, Restart }

@Serializable
internal data class BuildConfirmation(val build: BuildLaunchData, val kind: ConfirmationKind, val own: Boolean = true) {
    @get:StringRes val message: Int get() = when (kind) {
        ConfirmationKind.Stop -> if (own) R.string.text_stop_the_build else R.string.text_stop_the_build_2
        ConfirmationKind.Remove -> if (own) R.string.text_remove_build_from_queue else R.string.text_remove_build_from_queue_2
        ConfirmationKind.Restart -> R.string.text_restart_the_build
    }

    @get:StringRes val confirmLabel: Int get() = when (kind) {
        ConfirmationKind.Stop -> R.string.text_stop_button
        ConfirmationKind.Remove -> R.string.text_remove_from_queue_button
        ConfirmationKind.Restart -> R.string.text_restart_button
    }
}
internal data class BuildDetailsMenuAction(val action: BuildDetailsAction, @StringRes val label: Int)

@Serializable
internal data class BuildDetailsNotice(val id: Long, @StringRes val message: Int, val openQueued: Boolean = false) {
    @get:StringRes val actionLabel: Int? get() = when {
        !openQueued -> null
        message == R.string.error_opening_build -> R.string.text_retry
        else -> R.string.text_show_build
    }
}
internal data class BuildDetailsReplacement(val id: Long, val build: BuildLaunchData, val completion: BuildDetailsCompletion? = null)
internal data class BuildDetailsUiState(
    val incoming: BuildLaunchData,
    val current: BuildLaunchData = incoming,
    val configurationName: String? = null,
    val confirmation: BuildConfirmation? = null,
    val operation: BuildDetailsOperation? = null,
    val preparingConfirmation: Boolean = false,
    val notice: BuildDetailsNotice? = null,
    val replacement: BuildDetailsReplacement? = null,
    val overviewRefresh: Long = 0,
    val countFailures: Set<BuildDetailsSection> = emptySet(),
    val prompt: BuildDetailsPromptUi? = null,
    val onboardingFailed: Boolean = false
) {
    val title: String get() = current.number?.let { "#$it" } ?: "#${current.id}"
    val subtitle: String get() = current.configuration?.name ?: configurationName.orEmpty()
    val busy: Boolean get() = operation != null || preparingConfirmation
    val actions: List<BuildDetailsAction> get() = buildList {
        if (!current.webUrl.isNullOrBlank()) {
            add(BuildDetailsAction.Share)
            add(BuildDetailsAction.Browser)
        }
        add(if (current.isQueued || current.isRunning) BuildDetailsAction.Cancel else BuildDetailsAction.Restart)
        if (!current.buildTypeId.isNullOrBlank()) add(BuildDetailsAction.Configuration)
        if (!current.configuration?.projectId.isNullOrBlank()) add(BuildDetailsAction.Project)
    }
    val floatingActions: List<BuildDetailsMenuAction> get() = menu.filter { it.action == BuildDetailsAction.Share || it.action == BuildDetailsAction.Browser }
    val actionLoadingLabelRes: Int get() = operation?.message ?: if (preparingConfirmation) {
        R.string.preparing_confirmation
    } else {
        when {
            current.isRunning -> R.string.loading_stop
            current.isQueued -> R.string.loading_remove
            else -> R.string.loading_restart
        }
    }

    @get:StringRes val actionTooltipLabelRes: Int get() = if (busy) actionLoadingLabelRes else primaryAction.label
    val primaryAction: BuildDetailsMenuAction get() = when {
        current.isRunning -> BuildDetailsMenuAction(BuildDetailsAction.Cancel, R.string.action_stop_build)
        current.isQueued -> BuildDetailsMenuAction(BuildDetailsAction.Cancel, R.string.text_remove_from_queue_button)
        else -> BuildDetailsMenuAction(BuildDetailsAction.Restart, R.string.action_restart_build)
    }
    val menu: List<BuildDetailsMenuAction> get() = actions.map { BuildDetailsMenuAction(it, it.label(current)) }
}
internal fun BuildDetailsSection.label(): Int = when (this) {
    BuildDetailsSection.Changes -> R.string.tab_changes
    BuildDetailsSection.Tests -> R.string.tab_tests
    BuildDetailsSection.Parameters -> R.string.tab_parameters
    BuildDetailsSection.Artifacts -> R.string.tab_artifacts
    BuildDetailsSection.Snapshots -> R.string.tab_snapshot_dependencies
}
internal fun BuildDetailsAction.label(build: BuildLaunchData): Int = when (this) {
    BuildDetailsAction.Share -> R.string.action_share
    BuildDetailsAction.Browser -> R.string.action_browser
    BuildDetailsAction.Log -> R.string.action_view_log
    BuildDetailsAction.Cancel -> if (build.isRunning) R.string.text_stop_button else R.string.text_remove_from_queue_button
    BuildDetailsAction.Restart -> R.string.text_restart_button
    BuildDetailsAction.Configuration -> R.string.action_configuration
    BuildDetailsAction.Project -> R.string.action_project
}
