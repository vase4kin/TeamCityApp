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

package teamcityapp.features.build_details.impl.data

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.*

class TeamCityBuildDetailsRepository(private val account: AccountScope, private val api: TeamCityApi) : BuildDetailsRepository {
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
