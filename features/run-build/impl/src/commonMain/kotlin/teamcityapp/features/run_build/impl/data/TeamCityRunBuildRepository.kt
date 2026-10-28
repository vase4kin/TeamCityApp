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

package teamcityapp.features.run_build.impl.data

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import teamcityapp.features.run_build.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityRunBuildRepository(private val account: AccountScope, private val api: TeamCityApi) : RunBuildRepository {
    override suspend fun branches(buildTypeId: String): List<String> = account.run {
        api.branches(buildTypeId)
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
            values += page.items("agent").map { BuildAgent(it.text("id").orEmpty(), it.text("name").orEmpty()) }
            next = page.text("nextHref")
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
