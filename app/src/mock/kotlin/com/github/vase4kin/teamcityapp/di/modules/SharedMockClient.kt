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

package com.github.vase4kin.teamcityapp.di.modules

import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.rx2.await
import retrofit2.HttpException

/** Mock wire boundary only: the production common parser, guards, repositories and UI still run. */
internal fun sharedMockClient(service: () -> TeamCityService): HttpClient = HttpClient(
    MockEngine { request ->
        val api = service()
        val gson = Gson()
        val path = request.url.encodedPath
        val endpoint = path.substringAfter("/app/rest/")
        val query = request.url.parameters
        fun link() = path + request.url.encodedQuery.takeIf { it.isNotEmpty() }?.let { "?$it" }.orEmpty()
        fun id() = endpoint.substringAfter("id:").substringBefore('/')
        try {
            val result: Any = when {
                request.method == HttpMethod.Post -> {
                    val body = (request.body as? TextContent)?.text ?: error("Missing mock mutation payload")
                    if (endpoint == "buildQueue") {
                        api.queueBuild(gson.fromJson(body, Build::class.java)).await()
                    } else {
                        api.cancelBuild(path, gson.fromJson(body, BuildCancelRequest::class.java)).await()
                    }
                }

                endpoint == "server" -> api.serverInfo().await()

                endpoint.startsWith("projects/") -> api.listBuildTypes(id()).await()

                endpoint.startsWith("buildTypes/") && endpoint.endsWith("/builds") -> api.listBuilds(id(), query["locator"].orEmpty()).await()

                endpoint.startsWith("buildTypes/") && endpoint.endsWith("/branches") -> api.listBranches(id()).await()

                endpoint.startsWith("buildTypes/") -> api.buildType(id()).await()

                "/artifacts/content/" in endpoint -> {
                    val bytes = api.downloadFile(path).await().use { it.bytes() }
                    return@MockEngine respond(bytes, HttpStatusCode.OK, headersOf(HttpHeaders.ContentLength, bytes.size.toString()))
                }

                "/artifacts/" in endpoint -> api.listArtifacts(path, query["locator"].orEmpty()).await()

                endpoint.startsWith("builds/id:") || endpoint.startsWith("buildQueue/id:") -> api.build(path).await()

                endpoint == "buildQueue" -> api.listQueueBuilds(query["locator"], query["fields"]).await()

                endpoint == "builds" && "running:true" in query["locator"].orEmpty() -> api.listRunningBuilds(query["locator"].orEmpty(), query["fields"]).await()

                endpoint == "builds" -> api.listBuilds(query["locator"].orEmpty()).await()

                endpoint == "agents" -> api.listAgents(query["includeDisconnected"]?.toBoolean(), query["fields"], query["locator"]).await()

                endpoint == "changes" -> api.listChanges(link()).await()

                endpoint.startsWith("changes/") -> api.change(link()).await()

                endpoint == "testOccurrences" -> api.listTestOccurrences(link()).await()

                endpoint.startsWith("testOccurrences/") -> api.testOccurrence(link()).await()

                else -> return@MockEngine respond("{}", HttpStatusCode.NotFound)
            }
            respond(gson.toJson(result), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        } catch (failure: HttpException) {
            respond("{}", HttpStatusCode.fromValue(failure.code()))
        }
    }
) {
    followRedirects = false
    install(HttpTimeout)
}
