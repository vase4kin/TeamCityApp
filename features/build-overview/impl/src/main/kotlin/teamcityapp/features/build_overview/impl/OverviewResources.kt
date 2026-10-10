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

package teamcityapp.features.build_overview.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import teamcityapp.features.build_overview.api.BuildOverviewAction

@StringRes internal fun OverviewField.label(): Int = when (this) {
    OverviewField.Result -> R.string.overview_result
    OverviewField.WaitReason -> R.string.overview_wait_reason
    OverviewField.CancelledBy -> R.string.overview_cancelled_by
    OverviewField.CancellationTime -> R.string.overview_cancellation_time
    OverviewField.Time -> R.string.overview_time
    OverviewField.QueuedTime -> R.string.overview_queued_time
    OverviewField.EstimatedTime -> R.string.overview_estimated_time
    OverviewField.Branch -> R.string.overview_branch
    OverviewField.Agent -> R.string.overview_agent
    OverviewField.TriggeredBy -> R.string.overview_triggered_by
    OverviewField.RestartedBy -> R.string.overview_restarted_by
    OverviewField.Personal -> R.string.overview_personal
    OverviewField.Configuration -> R.string.overview_configuration
    OverviewField.Project -> R.string.overview_project
}

@StringRes internal fun BuildOverviewAction.label(): Int = when (this) {
    BuildOverviewAction.Share -> R.string.overview_share
    BuildOverviewAction.Browser -> R.string.overview_browser
    BuildOverviewAction.Stop -> R.string.overview_stop
    BuildOverviewAction.RemoveFromQueue -> R.string.overview_remove
    BuildOverviewAction.Restart -> R.string.overview_restart
    BuildOverviewAction.Configuration -> R.string.overview_configuration
    BuildOverviewAction.Project -> R.string.overview_project
}
internal fun OverviewText.resource(): Int = when (this) {
    OverviewText.DeletedUser -> R.string.overview_deleted_user
    OverviewText.DeletedConfiguration -> R.string.overview_deleted_configuration
    OverviewText.UnknownTrigger -> R.string.overview_unknown_trigger
    OverviewText.QueuedBuild -> R.string.overview_queued_build
    OverviewText.Unavailable -> R.string.overview_unavailable
    is OverviewText.Literal -> error("Literal text has no resource")
}

@Composable internal fun OverviewText.resolve(): String = when (this) {
    is OverviewText.Literal -> value
    else -> stringResource(resource())
}
