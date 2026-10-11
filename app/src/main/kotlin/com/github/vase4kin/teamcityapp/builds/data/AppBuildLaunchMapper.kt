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

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.User
import com.github.vase4kin.teamcityapp.navigation.api.BuildType
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import javax.inject.Inject
import teamcityapp.libraries.builds.*

/** Explicit boundary between immutable launch snapshots and the installed serialized Build DTO. */
class AppBuildLaunchMapper @Inject constructor() {
    private val gson = Gson()

    fun toLaunchData(build: Build): BuildLaunchData {
        // The legacy NonNull annotations do not prevent partial server DTOs from containing null.
        val id: String? = build.id
        val href: String? = build.href
        return BuildLaunchData(
            id = requireNotNull(id) { "A build launch requires an ID" },
            href = requireNotNull(href) { "A build launch requires a detail URL" },
            webUrl = build.webUrl,
            number = build.number,
            state = build.state,
            status = build.status,
            statusText = build.statusText,
            branchName = build.branchName,
            buildTypeId = build.buildTypeId,
            configuration = build.buildType?.let(::configuration),
            queuedDate = build.queuedDate,
            startDate = build.startDate,
            finishDate = build.finishDate,
            waitReason = build.waitReason,
            startEstimate = build.startEstimate,
            triggered = build.triggered?.let {
                BuildTrigger(it.type, it.date, it.details, it.user?.let(::user), it.buildType?.let(::configuration))
            },
            agent = build.agent?.let { BuildAgent(it.id, it.name, it.href) },
            canceledInfo = build.canceledInfo?.let { BuildCancellation(it.timestamp, it.user?.let(::user)) },
            changes = build.changes?.let { BuildCollectionLink(it.href, it.count, it.nextHref) },
            artifacts = build.artifacts?.let { BuildCollectionLink(it.href) },
            tests = build.testOccurrences?.let { BuildTests(it.href, it.passed, it.failed, it.ignored, it.count, it.nextHref) },
            properties = build.properties?.let { wrapper ->
                BuildProperties(wrapper.properties?.map { BuildProperty(it.name, it.value, it.isOwn, it.id, it.href) }, wrapper.id, wrapper.href)
            },
            snapshotDependencies = build.snapshotBuilds?.let { BuildCollectionLink(it.href, it.count, it.nextHref) },
            personal = build.isPersonal,
            pinned = build.isPinned,
            cleanSources = build.isCleanSources,
            queueAtTop = build.isQueueAtTop
        )
    }

    fun toLegacyBuild(build: BuildLaunchData): Build = gson.fromJson(build.json(), Build::class.java)

    private fun configuration(value: BuildType) = BuildConfigurationData(value.id, value.name, value.projectId, value.projectName, value.href)
    private fun user(value: User) = BuildUser(value.username, value.name, value.id, value.href)

    private fun BuildLaunchData.json() = JsonObject().apply {
        string("id", id)
        string("href", href)
        string("webUrl", webUrl)
        string("number", number)
        string("state", state)
        string("status", status)
        string("statusText", statusText)
        string("branchName", branchName)
        string("buildTypeId", buildTypeId)
        add("buildType", configuration?.json().orNull())
        string("queuedDate", queuedDate)
        string("startDate", startDate)
        string("finishDate", finishDate)
        string("waitReason", waitReason)
        string("startEstimate", startEstimate)
        add("triggered", triggered?.json().orNull())
        add("agent", agent?.json().orNull())
        add("canceledInfo", canceledInfo?.json().orNull())
        add("changes", changes?.json().orNull())
        add("artifacts", artifacts?.json().orNull())
        add("testOccurrences", tests?.json().orNull())
        add("properties", properties?.json().orNull())
        add("snapshot-dependencies", snapshotDependencies?.json().orNull())
        addProperty("personal", personal)
        addProperty("pinned", pinned)
        addProperty("cleanSources", cleanSources)
        addProperty("queueAtTop", queueAtTop)
    }

    private fun BuildConfigurationData.json() = JsonObject().apply {
        string("id", id)
        string("href", href)
        string("name", name)
        string("projectId", projectId)
        string("projectName", projectName)
    }

    private fun BuildUser.json() = JsonObject().apply {
        string("id", id)
        string("href", href)
        string("username", username)
        string("name", name)
    }

    private fun BuildTrigger.json() = JsonObject().apply {
        string("type", type)
        string("date", date)
        string("details", details)
        add("user", user?.json().orNull())
        add("buildType", configuration?.json().orNull())
    }

    private fun BuildAgent.json() = JsonObject().apply {
        string("id", id)
        string("href", href)
        string("name", name)
    }

    private fun BuildCancellation.json() = JsonObject().apply {
        string("timestamp", timestamp)
        add("user", user?.json().orNull())
    }

    private fun BuildCollectionLink.json() = JsonObject().apply {
        string("href", href)
        if (count == null) add("count", JsonNull.INSTANCE) else addProperty("count", count)
        string("nextHref", nextHref)
    }

    private fun BuildTests.json() = JsonObject().apply {
        string("href", href)
        addProperty("passed", passed)
        addProperty("failed", failed)
        addProperty("ignored", ignored)
        addProperty("count", count)
        string("nextHref", nextHref)
    }

    private fun BuildProperties.json() = JsonObject().apply {
        string("id", id)
        string("href", href)
        add("property", items?.let { values -> JsonArray().apply { values.forEach { add(it.json()) } } }.orNull())
    }

    private fun BuildProperty.json() = JsonObject().apply {
        string("id", id)
        string("href", href)
        string("name", name)
        string("value", value)
        addProperty("own", own)
    }

    private fun JsonObject.string(key: String, value: String?) {
        if (value == null) add(key, JsonNull.INSTANCE) else addProperty(key, value)
    }

    private fun JsonElement?.orNull(): JsonElement = this ?: JsonNull.INSTANCE
}
