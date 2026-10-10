/*
 * Copyright 2020 Andrey Tolpeev
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

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import teamcityapp.libraries.builds.*

enum class OverviewField { Result, WaitReason, CancelledBy, CancellationTime, Time, QueuedTime, EstimatedTime, Branch, Agent, TriggeredBy, RestartedBy, Personal, Configuration, Project }
enum class OverviewIcon { Success, Failure, Error, Unknown, Running, Queued, Time, Branch, Agent, Person, Configuration, Project }
enum class OverviewRowAction { Copy, Branch, Configuration, Project }
sealed interface OverviewText {
    data class Literal(val value: String) : OverviewText
    data object DeletedUser : OverviewText
    data object DeletedConfiguration : OverviewText
    data object UnknownTrigger : OverviewText
    data object QueuedBuild : OverviewText
    data object Unavailable : OverviewText
}
data class OverviewRow(val field: OverviewField, val value: OverviewText, val icon: OverviewIcon, val action: OverviewRowAction = OverviewRowAction.Copy)

internal fun overviewRows(build: BuildLaunchData): List<OverviewRow> = buildList {
    fun add(field: OverviewField, value: OverviewText, icon: OverviewIcon, action: OverviewRowAction = OverviewRowAction.Copy) {
        add(OverviewRow(field, value, icon, action))
    }
    fun addText(field: OverviewField, value: String?, icon: OverviewIcon, action: OverviewRowAction = OverviewRowAction.Copy) {
        if (value != null) add(field, OverviewText.Literal(value), icon, action)
    }
    val statusIcon = when {
        build.isRunning -> OverviewIcon.Running
        build.isQueued -> OverviewIcon.Queued
        build.status == "FAILURE" -> OverviewIcon.Failure
        build.status == "ERROR" -> OverviewIcon.Error
        build.status == "UNKNOWN" -> OverviewIcon.Unknown
        else -> OverviewIcon.Success
    }
    add(
        if (build.isQueued) OverviewField.WaitReason else OverviewField.Result,
        if (build.isQueued) build.waitReason?.let { OverviewText.Literal(it) } ?: OverviewText.QueuedBuild else build.statusText?.let { OverviewText.Literal(it) } ?: OverviewText.Unavailable,
        statusIcon
    )
    build.canceledInfo?.let { cancellation ->
        cancellation.user?.let { add(OverviewField.CancelledBy, userText(it), statusIcon) }
        add(OverviewField.CancellationTime, dateText(cancellation.timestamp), OverviewIcon.Time)
    }
    when {
        build.isRunning -> add(OverviewField.Time, dateText(build.startDate), OverviewIcon.Time)
        build.isQueued -> add(OverviewField.QueuedTime, dateText(build.queuedDate), OverviewIcon.Time)
        else -> add(OverviewField.Time, durationText(build.startDate, build.finishDate), OverviewIcon.Time)
    }
    if (build.isQueued && !build.startEstimate.isNullOrEmpty()) add(OverviewField.EstimatedTime, dateText(build.startEstimate), OverviewIcon.Time)
    if (!build.branchName.isNullOrEmpty()) addText(OverviewField.Branch, build.branchName, OverviewIcon.Branch, OverviewRowAction.Branch)
    build.agent?.let { addText(OverviewField.Agent, it.name, OverviewIcon.Agent) }
    val trigger = build.triggered
    when (trigger?.type) {
        "vcs", "unknown" -> addText(OverviewField.TriggeredBy, trigger.details, OverviewIcon.Person)
        "user" -> add(OverviewField.TriggeredBy, userText(trigger.user), OverviewIcon.Person)
        "restarted" -> add(OverviewField.RestartedBy, userText(trigger.user), OverviewIcon.Person)
        "buildType" -> add(OverviewField.TriggeredBy, trigger.configuration?.let { OverviewText.Literal("${it.projectName} ${it.name}") } ?: OverviewText.DeletedConfiguration, OverviewIcon.Person)
        else -> add(OverviewField.TriggeredBy, OverviewText.UnknownTrigger, OverviewIcon.Person)
    }
    if (build.personal) add(OverviewField.Personal, userText(trigger?.user), OverviewIcon.Person)
    val configuration = build.configuration
    if (configuration?.projectName != null) {
        addText(OverviewField.Configuration, configuration.name, OverviewIcon.Configuration, OverviewRowAction.Configuration)
        addText(OverviewField.Project, configuration.projectName, OverviewIcon.Project, OverviewRowAction.Project)
    }
}
private fun userText(user: BuildUser?): OverviewText = if (user == null) OverviewText.DeletedUser else (user.name ?: user.username)?.let { OverviewText.Literal(it) } ?: OverviewText.DeletedUser
private fun parseDate(raw: String?): Date? = raw?.let {
    try {
        SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).apply { isLenient = false }.parse(it)
    } catch (_: Exception) {
        null
    }
}
private fun formatDate(date: Date, pattern: String): String = SimpleDateFormat(pattern, Locale.US).format(date)
private fun dateText(raw: String?): OverviewText = parseDate(raw)?.let { OverviewText.Literal(formatDate(it, "dd MMM yy HH:mm")) } ?: OverviewText.Unavailable
private fun durationText(start: String?, finish: String?): OverviewText {
    val from = parseDate(start) ?: return OverviewText.Unavailable
    val to = parseDate(finish) ?: return OverviewText.Unavailable
    var seconds = ((to.time - from.time) / 1000).coerceAtLeast(0)
    val duration = buildString {
        val days = seconds / 86400
        seconds %= 86400
        val hours = seconds / 3600
        seconds %= 3600
        val minutes = seconds / 60
        seconds %= 60
        if (days != 0L) append("${days}d:")
        if (hours != 0L) append("${hours}h:")
        if (minutes != 0L) append("${minutes}m:")
        if (seconds != 0L || isEmpty()) append("${seconds}s")
    }
    return OverviewText.Literal("${formatDate(from, "dd MMM yy HH:mm")} - ${formatDate(to, "HH:mm")} ($duration)")
}
