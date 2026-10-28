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
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/** Mock wire boundary only: the production common parser, guards, repositories and UI still run. */
internal fun sharedMockClient(service: () -> TeamCityService): HttpClient = HttpClient(
    MockEngine { request ->
        val api = service()
        val gson = Gson()
        val path = request.url.encodedPath
        val endpoint = path.substringAfter("/app/rest/")
        val query = request.url.parameters

        // Legacy fixtures key their responses by decoded query values. Keep that
        // translation here; the common transport still sends encoded wire URLs.
        fun link() = path + query.entries().flatMap { (key, values) -> values.map { "$key=$it" } }
            .joinToString("&").takeIf(String::isNotEmpty)?.let { "?$it" }.orEmpty()
        fun id() = endpoint.substringAfter("id:").substringBefore('/')
        try {
            val result: Any = when {
                request.method == HttpMethod.Post -> {
                    val body = (request.body as? TextContent)?.text ?: error("Missing mock mutation payload")
                    if (endpoint == "buildQueue") {
                        api.queueBuild(gson.fromJson(body, Build::class.java))
                    } else {
                        val reAdd = JsonParser.parseString(body).asJsonObject["readdIntoQueue"]?.asBoolean ?: false
                        api.cancelBuild(path, BuildCancelRequest(reAdd))
                    }
                }

                endpoint == "server" -> api.serverInfo()

                endpoint.startsWith("projects/") -> api.listBuildTypes(id())

                endpoint.startsWith("buildTypes/") && endpoint.endsWith("/builds") -> api.listBuilds(id(), query["locator"].orEmpty())

                endpoint.startsWith("buildTypes/") && endpoint.endsWith("/branches") -> api.listBranches(id())

                endpoint.startsWith("buildTypes/") -> api.buildType(id())

                "/artifacts/content/" in endpoint -> {
                    val bytes = api.downloadFile(path).use { body -> runInterruptible(Dispatchers.IO) { body.bytes() } }
                    return@MockEngine respond(bytes, HttpStatusCode.OK, headersOf(HttpHeaders.ContentLength, bytes.size.toString()))
                }

                "/artifacts/" in endpoint -> api.listArtifacts(path, query["locator"].orEmpty())

                endpoint.startsWith("builds/id:") || endpoint.startsWith("buildQueue/id:") -> api.build(path)

                endpoint == "buildQueue" -> api.listQueueBuilds(query["locator"], query["fields"])

                endpoint == "builds" && "running:true" in query["locator"].orEmpty() -> api.listRunningBuilds(query["locator"].orEmpty(), query["fields"])

                endpoint == "builds" -> api.listBuilds(query["locator"].orEmpty())

                endpoint == "agents" -> api.listAgents(query["includeDisconnected"]?.toBoolean(), query["fields"], query["locator"])

                endpoint == "changes" -> api.listChanges(link())

                endpoint.startsWith("changes/") -> api.change(link())

                endpoint == "testOccurrences" -> api.listTestOccurrences(link())

                endpoint.startsWith("testOccurrences/") -> api.testOccurrence(link())

                else -> return@MockEngine respond("{}", HttpStatusCode.NotFound)
            }
            respond(gson.toJson(result), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        } catch (failure: MockHttpFailure) {
            respond("{}", HttpStatusCode.fromValue(failure.status))
        }
    }
) {
    followRedirects = false
    install(HttpTimeout)
}
