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

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.resources.*
import teamcityapp.libraries.builds.BuildLaunchData

typealias BuildDetailsAction = BuildDetailsCommand
enum class BuildDetailsOperation(val message: StringResource) {
    Stop(Res.string.loading_stop),
    Remove(Res.string.loading_remove),
    Restart(Res.string.loading_restart)
}

@Serializable
enum class ConfirmationKind { Stop, Remove, Restart }

@Serializable
data class BuildConfirmation(val build: BuildLaunchData, val kind: ConfirmationKind, val own: Boolean = true) {
    val message: StringResource get() = when (kind) {
        ConfirmationKind.Stop -> if (own) Res.string.text_stop_the_build else Res.string.text_stop_the_build_2
        ConfirmationKind.Remove -> if (own) Res.string.text_remove_build_from_queue else Res.string.text_remove_build_from_queue_2
        ConfirmationKind.Restart -> Res.string.text_restart_the_build
    }

    val confirmLabel: StringResource get() = when (kind) {
        ConfirmationKind.Stop -> Res.string.text_stop_button
        ConfirmationKind.Remove -> Res.string.text_remove_from_queue_button
        ConfirmationKind.Restart -> Res.string.text_restart_button
    }
}
data class BuildDetailsMenuAction(val action: BuildDetailsAction, val label: StringResource)

@Serializable
data class BuildDetailsNotice(val id: Long, val text: BuildDetailsText, val openQueued: Boolean = false) {
    constructor(id: Long, message: StringResource, openQueued: Boolean = false) : this(id, BuildDetailsText.entries.single { it.resource == message }, openQueued)
    val message: StringResource get() = text.resource
    val actionLabel: StringResource? get() = when {
        !openQueued -> null
        message == Res.string.error_opening_build -> Res.string.text_retry
        else -> Res.string.text_show_build
    }
}
data class BuildDetailsReplacement(val id: Long, val build: BuildLaunchData, val completion: BuildDetailsCompletion? = null)
data class BuildDetailsUiState(
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
    val onboardingFailed: Boolean = false,
    val platformActionsAvailable: Boolean = true
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
    val floatingActions: List<BuildDetailsMenuAction> get() = menu.filter { platformActionsAvailable && (it.action == BuildDetailsAction.Share || it.action == BuildDetailsAction.Browser) }
    val actionLoadingLabelRes: StringResource get() = operation?.message ?: if (preparingConfirmation) {
        Res.string.preparing_confirmation
    } else {
        when {
            current.isRunning -> Res.string.loading_stop
            current.isQueued -> Res.string.loading_remove
            else -> Res.string.loading_restart
        }
    }

    val actionTooltipLabelRes: StringResource get() = if (busy) actionLoadingLabelRes else primaryAction.label
    val primaryAction: BuildDetailsMenuAction get() = when {
        current.isRunning -> BuildDetailsMenuAction(BuildDetailsAction.Cancel, Res.string.action_stop_build)
        current.isQueued -> BuildDetailsMenuAction(BuildDetailsAction.Cancel, Res.string.text_remove_from_queue_button)
        else -> BuildDetailsMenuAction(BuildDetailsAction.Restart, Res.string.action_restart_build)
    }
    val menu: List<BuildDetailsMenuAction> get() = actions.map { BuildDetailsMenuAction(it, it.label(current)) }
}
fun BuildDetailsSection.label(): StringResource = when (this) {
    BuildDetailsSection.Changes -> Res.string.tab_changes
    BuildDetailsSection.Tests -> Res.string.tab_tests
    BuildDetailsSection.Parameters -> Res.string.tab_parameters
    BuildDetailsSection.Artifacts -> Res.string.tab_artifacts
    BuildDetailsSection.Snapshots -> Res.string.tab_snapshot_dependencies
}
fun BuildDetailsAction.label(build: BuildLaunchData): StringResource = when (this) {
    BuildDetailsAction.Share -> Res.string.action_share
    BuildDetailsAction.Browser -> Res.string.action_browser
    BuildDetailsAction.Log -> Res.string.action_view_log
    BuildDetailsAction.Cancel -> if (build.isRunning) Res.string.text_stop_button else Res.string.text_remove_from_queue_button
    BuildDetailsAction.Restart -> Res.string.text_restart_button
    BuildDetailsAction.Configuration -> Res.string.action_configuration
    BuildDetailsAction.Project -> Res.string.action_project
}

@Serializable
enum class BuildDetailsText {
    TextBuildIsStopped,
    TextBuildIsRemovedFromQueue,
    TextBuildIsRestarted,
    ErrorRestartBuildForbiddenError,
    ErrorBaseRestartBuildError,
    ErrorStopBuildForbiddenError,
    ErrorRemoveBuildFromQueueForbiddenError,
    ErrorBaseStopBuildError,
    ErrorBaseRemoveBuildFromQueueError,
    ErrorOpeningBuild;

    val resource: StringResource get() = when (this) {
        TextBuildIsStopped -> Res.string.text_build_is_stopped
        TextBuildIsRemovedFromQueue -> Res.string.text_build_is_removed_from_queue
        TextBuildIsRestarted -> Res.string.text_build_is_restarted
        ErrorRestartBuildForbiddenError -> Res.string.error_restart_build_forbidden_error
        ErrorBaseRestartBuildError -> Res.string.error_base_restart_build_error
        ErrorStopBuildForbiddenError -> Res.string.error_stop_build_forbidden_error
        ErrorRemoveBuildFromQueueForbiddenError -> Res.string.error_remove_build_from_queue_forbidden_error
        ErrorBaseStopBuildError -> Res.string.error_base_stop_build_error
        ErrorBaseRemoveBuildFromQueueError -> Res.string.error_base_remove_build_from_queue_error
        ErrorOpeningBuild -> Res.string.error_opening_build
    }
}
