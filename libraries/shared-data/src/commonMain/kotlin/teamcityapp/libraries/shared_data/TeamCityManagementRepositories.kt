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

package teamcityapp.libraries.shared_data

import io.ktor.http.encodeURLQueryComponent
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.filter_builds.api.FilterBuildsRepository
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.accounts.AccountScope
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.*

/** Every operation belongs to the captured account generation; no mutation is automatically retried. */
class TeamCityManagementRepositories(private val account: AccountScope, private val api: TeamCityApi) {
    val filters = object : FilterBuildsRepository {
        override suspend fun branches(buildTypeId: String): List<String> = run.branches(buildTypeId)
    }
    val run = object : RunBuildRepository {
        override suspend fun branches(buildTypeId: String): List<String> = account.run {
            api.document(api.apiPath("buildTypes/id:${buildTypeId.entityId()}/branches?locator=policy:ALL_BRANCHES")).objects("branch").mapNotNull { it.string("name") }
        }
        override suspend fun agents(buildTypeId: String): List<BuildAgent> = account.run {
            val config = api.document(api.apiPath("buildTypes/id:${buildTypeId.entityId()}?fields=compatibleAgents"))
            val query = if (config["compatibleAgents"] is JsonObject) "locator=" + "compatible:(buildType:(id:$buildTypeId))".encoded() else "includeDisconnected=false"
            val values = mutableListOf<BuildAgent>()
            var next: String? = api.apiPath("agents?$query&fields=agent(id,name),nextHref")
            val seen = mutableSetOf<String>()
            while (next != null) {
                check(seen.add(next)) { "Repeated agent continuation" }
                val page = api.document(next)
                values += page.objects("agent").map { BuildAgent(it.string("id").orEmpty(), it.string("name").orEmpty()) }
                next = page.string("nextHref")
            }
            values
        }
        override suspend fun queue(request: BuildRequest): QueueBuildResult = try {
            val href = account.run { api.mutate(api.apiPath("buildQueue"), request.payload()).requiredHref() }
            QueueBuildResult.Success(href)
        } catch (c: CancellationException) {
            throw c
        } catch (e: TeamCityHttpFailure) {
            if (e.status == 403) QueueBuildResult.Forbidden else QueueBuildResult.Error
        } catch (_: Exception) {
            QueueBuildResult.Error
        }
    }
    val details = object : BuildDetailsRepository {
        override suspend fun triggeredByCurrentUser(build: BuildLaunchData): Boolean = account.run {
            build.triggered?.let { it.type == "user" && it.user?.username == account.identity.userName } == true
        }
        override suspend fun cancel(build: BuildLaunchData, reAddToQueue: Boolean): BuildDetailsMutation<BuildLaunchData> = mutation {
            val response = api.mutate(build.href, buildJsonObject { put("readdIntoQueue", reAddToQueue && build.isRunning) })
            // A short mutation acknowledgement must never enter the normal details host.
            api.build(response.requiredHref(), true)
        }
        override suspend fun restart(build: BuildLaunchData): BuildDetailsMutation<String> = mutation {
            val payload = buildJsonObject {
                put("buildType", buildJsonObject { put("id", requireNotNull(build.buildTypeId).entityId()) })
                build.branchName?.let { put("branchName", it) }
                build.properties?.let { props ->
                    put(
                        "properties",
                        buildJsonObject {
                            put(
                                "property",
                                JsonArray(
                                    props.items.orEmpty().map { property ->
                                        buildJsonObject {
                                            property.name?.let { put("name", it) }
                                            property.value?.let { put("value", it) }
                                        }
                                    }
                                )
                            )
                        }
                    )
                }
            }
            api.mutate(api.apiPath("buildQueue"), payload).requiredHref()
        }
        override suspend fun queuedBuild(href: String): BuildLaunchData = account.run { api.build(href, true) }
    }
    private suspend fun <T> mutation(block: suspend () -> T): BuildDetailsMutation<T> = try {
        BuildDetailsMutation.Success(account.run { block() })
    } catch (c: CancellationException) {
        throw c
    } catch (e: TeamCityHttpFailure) {
        if (e.status == 403) BuildDetailsMutation.Forbidden else BuildDetailsMutation.Failed
    } catch (_: Exception) {
        BuildDetailsMutation.Failed
    }
}

internal fun BuildRequest.payload(): JsonObject = buildJsonObject {
    put("buildType", buildJsonObject { put("id", buildTypeId.entityId()) })
    put("branchName", branch)
    put("personal", personal)
    put(
        "triggeringOptions",
        buildJsonObject {
            put("queueAtTop", queueAtTop)
            put("cleanSources", cleanSources)
        }
    )
    agent?.let {
        put(
            "agent",
            buildJsonObject {
                put("id", it.id)
                put("name", it.name)
            }
        )
    }
    if (parameters.isNotEmpty()) {
        put(
            "properties",
            buildJsonObject {
                put(
                    "property",
                    JsonArray(
                        parameters.map {
                            buildJsonObject {
                                put("name", it.name)
                                put("value", it.value)
                            }
                        }
                    )
                )
            }
        )
    }
}
private fun JsonObject.objects(key: String): List<JsonObject> = (this[key] as? JsonArray)?.map { it.jsonObject }.orEmpty()
private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
private fun JsonObject.requiredHref(): String = requireNotNull(string("href")).also { require(it.isNotBlank()) }
private fun String.encoded() = encodeURLQueryComponent(encodeFull = true)
private fun String.entityId(): String = apply { require(isNotBlank() && all { it.isLetterOrDigit() || it in "_- ." } && ' ' !in this) }
