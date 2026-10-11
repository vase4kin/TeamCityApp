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
    Stop(R.string.text_stopping_build),
    Remove(R.string.text_removing_build_from_queue),
    Restart(R.string.text_restarting_build),
    OpenQueued(R.string.text_opening_build)
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
internal data class BuildDetailsTabLabel(@StringRes val title: Int, @StringRes val format: Int, val count: Int)
internal data class BuildDetailsMenuAction(val action: BuildDetailsAction, @StringRes val label: Int)

@Serializable
internal data class BuildDetailsNotice(val id: Long, @StringRes val message: Int, val openQueued: Boolean = false)
internal data class BuildDetailsReplacement(val id: Long, val build: BuildLaunchData)
internal data class BuildDetailsUiState(
    val incoming: BuildLaunchData,
    val current: BuildLaunchData = incoming,
    val configurationName: String? = null,
    val tabs: List<BuildDetailsTab> = incoming.detailsTabs(),
    val selected: BuildDetailsTab = BuildDetailsTab.Overview,
    val counts: Map<BuildDetailsTab, Int> = emptyMap(),
    val confirmation: BuildConfirmation? = null,
    val operation: BuildDetailsOperation? = null,
    val preparingConfirmation: Boolean = false,
    val notice: BuildDetailsNotice? = null,
    val replacement: BuildDetailsReplacement? = null,
    val overviewRefresh: Long = 0,
    val countFailures: Set<BuildDetailsTab> = emptySet(),
    val prompt: BuildDetailsPromptUi? = null,
    val onboardingFailed: Boolean = false
) {
    val tabLabels get() = tabs.associateWith { tab ->
        BuildDetailsTabLabel(
            tab.label(),
            when {
                tab in countFailures -> R.string.tab_with_unavailable_count
                tab in counts -> R.string.tab_with_count
                else -> R.string.tab_plain
            },
            counts[tab] ?: 0
        )
    }
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
    val menu: List<BuildDetailsMenuAction> get() = actions.map { BuildDetailsMenuAction(it, it.label(current)) }
}
internal fun BuildDetailsTab.label(): Int = when (this) {
    BuildDetailsTab.Overview -> R.string.tab_overview
    BuildDetailsTab.Changes -> R.string.tab_changes
    BuildDetailsTab.Tests -> R.string.tab_tests
    BuildDetailsTab.BuildLog -> R.string.tab_build_log
    BuildDetailsTab.Parameters -> R.string.tab_parameters
    BuildDetailsTab.Artifacts -> R.string.tab_artifacts
    BuildDetailsTab.Snapshots -> R.string.tab_snapshot_dependencies
}
internal fun BuildDetailsAction.label(build: BuildLaunchData): Int = when (this) {
    BuildDetailsAction.Share -> R.string.action_share
    BuildDetailsAction.Browser -> R.string.action_browser
    BuildDetailsAction.Cancel -> if (build.isRunning) R.string.text_stop_button else R.string.text_remove_from_queue_button
    BuildDetailsAction.Restart -> R.string.text_restart_button
    BuildDetailsAction.Configuration -> R.string.action_configuration
    BuildDetailsAction.Project -> R.string.action_project
}
