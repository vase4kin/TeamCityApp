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
import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.build_overview.impl.resources.*
import teamcityapp.libraries.design_system.SharedUiText

fun OverviewField.label(): StringResource = when (this) {
    OverviewField.Result -> Res.string.overview_result
    OverviewField.WaitReason -> Res.string.overview_wait_reason
    OverviewField.CancelledBy -> Res.string.overview_cancelled_by
    OverviewField.CancellationTime -> Res.string.overview_cancellation_time
    OverviewField.Time -> Res.string.overview_time
    OverviewField.QueuedTime -> Res.string.overview_queued_time
    OverviewField.EstimatedTime -> Res.string.overview_estimated_time
    OverviewField.Branch -> Res.string.overview_branch
    OverviewField.Agent -> Res.string.overview_agent
    OverviewField.TriggeredBy -> Res.string.overview_triggered_by
    OverviewField.RestartedBy -> Res.string.overview_restarted_by
    OverviewField.Personal -> Res.string.overview_personal
    OverviewField.Configuration -> Res.string.overview_configuration
    OverviewField.Project -> Res.string.overview_project
}

fun OverviewText.resource(): StringResource = when (this) {
    OverviewText.DeletedUser -> Res.string.overview_deleted_user
    OverviewText.DeletedConfiguration -> Res.string.overview_deleted_configuration
    OverviewText.UnknownTrigger -> Res.string.overview_unknown_trigger
    OverviewText.QueuedBuild -> Res.string.overview_queued_build
    OverviewText.Unavailable -> Res.string.overview_unavailable
    is OverviewText.Literal -> error("Literal text has no resource")
}

fun OverviewText.toSharedUiText(): SharedUiText = when (this) {
    is OverviewText.Literal -> SharedUiText.Dynamic(value)
    else -> SharedUiText.Resource(resource())
}
