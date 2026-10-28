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

package teamcityapp.libraries.network

import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.parseQueryString
import kotlinx.serialization.json.JsonObject

private fun TeamCityApi.monitoringPaths(queued: Boolean, ids: List<String>?): List<String> {
    val base = if (queued) "" else "running:true,branch:default:any,personal:false,pinned:false"
    return (ids?.distinct()?.map { listOf(base, "buildType:${it.entityId()}").filter(String::isNotEmpty).joinToString(",") } ?: listOf(base)).map {
        apiPath(if (queued) "buildQueue" else "builds") + if (it.isEmpty()) "" else "?locator=${it.encoded()}"
    }
}
private fun withFields(path: String, fields: String) = path + (if ('?' in path) "&" else "?") + "fields=" + fields.encoded()
suspend fun TeamCityApi.countBuilds(queued: Boolean, ids: List<String>?) = monitoringPaths(queued, ids).sumOf { document(withFields(it, "count"), true).count() }
suspend fun TeamCityApi.monitoringBuilds(queued: Boolean, ids: List<String>?, forceRefresh: Boolean) = monitoringPaths(queued, ids).flatMap { fullBuilds(withFields(it, BUILD_FIELDS + ",nextHref"), forceRefresh) }.distinctBy { it.id }
suspend fun TeamCityApi.fullBuilds(path: String, forceRefresh: Boolean) = allDocuments(path, "build", forceRefresh).map { it.toBuildLaunchData() }
suspend fun TeamCityApi.allDocuments(path: String, key: String, forceRefresh: Boolean): List<JsonObject> {
    val visited = mutableSetOf<String>()
    val result = mutableListOf<JsonObject>()
    var next: String? = path
    while (next != null) {
        require(visited.add(next)) { "Repeated TeamCity continuation URL" }
        val page = document(next, forceRefresh)
        val items = page.items(key)
        result += items
        next = page.text("nextHref")?.takeIf { items.isNotEmpty() && it.isNotBlank() }
    }
    return result
}

/** Amend only the initial locator. Continuation URLs are passed through without reconstruction. */
fun collectionPath(url: String, suffix: String, fields: String? = null): String {
    val query = parseQueryString(url.substringAfter('?', ""))
    val locator = listOfNotNull(query["locator"]?.takeIf(String::isNotBlank), suffix).joinToString(",")
    val params = query.entries().filter { it.key != "locator" && (fields == null || it.key != "fields") }.flatMap { (key, values) -> values.map { key to it } } + listOf("locator" to locator) + (fields?.let { listOf("fields" to it) } ?: emptyList())
    return url.substringBefore('?') + "?" + params.joinToString("&") { (key, value) -> "${key.encoded()}=${value.encoded()}" }
}

fun JsonObject.count(key: String = "count") = text(key)?.toIntOrNull() ?: 0
fun String.encoded() = encodeURLQueryComponent(encodeFull = true)
fun String.entityId(): String = also { require(isNotBlank() && all { c -> c.isLetterOrDigit() || c in "_-." }) }
fun JsonObject.requiredHref(): String = requireNotNull(text("href")).also { require(it.isNotBlank()) }
suspend fun TeamCityApi.branches(buildTypeId: String): List<String> = document(apiPath("buildTypes/id:${buildTypeId.entityId()}/branches?locator=policy:ALL_BRANCHES")).items("branch").mapNotNull { it.text("name") }
