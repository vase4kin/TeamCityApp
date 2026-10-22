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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.builds.*

internal fun JsonObject.text(key: String): String? = (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content
internal fun JsonObject.number(key: String): Int? = text(key)?.toIntOrNull()
internal fun JsonObject.flag(key: String): Boolean = text(key)?.toBooleanStrictOrNull() ?: false
internal fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject
internal fun JsonObject.items(key: String): List<JsonObject> = (this[key] as? JsonArray)?.map { it.jsonObject }.orEmpty()

fun JsonObject.toProjectReference() = ProjectReference(requireNotNull(text("id")), text("name").orEmpty())
fun JsonObject.toConfigurationSummary() = BuildConfigurationSummary(
    requireNotNull(text("id")),
    text("name").orEmpty(),
    text("description"),
    ProjectReference(text("projectId").orEmpty(), text("projectName").orEmpty())
)

/** Explicit wire/domain mapping preserves absent wrappers versus present empty collections. */
fun JsonObject.toBuildLaunchData(): BuildLaunchData = BuildLaunchData(
    id = requireNotNull(text("id")), href = requireNotNull(text("href")),
    webUrl = text("webUrl"), number = text("number"), state = text("state"), status = text("status"),
    statusText = text("statusText"), branchName = text("branchName"), buildTypeId = text("buildTypeId"),
    configuration = obj("buildType")?.configuration(),
    queuedDate = text("queuedDate"), startDate = text("startDate"), finishDate = text("finishDate"),
    waitReason = text("waitReason"), startEstimate = text("startEstimate"),
    triggered = obj("triggered")?.let { BuildTrigger(it.text("type"), it.text("date"), it.text("details"), it.obj("user")?.user(), it.obj("buildType")?.configuration()) },
    agent = obj("agent")?.let { BuildAgent(it.text("id"), it.text("name"), it.text("href")) },
    canceledInfo = obj("canceledInfo")?.let { BuildCancellation(it.text("timestamp"), it.obj("user")?.user()) },
    changes = obj("changes")?.link(), artifacts = obj("artifacts")?.link(),
    tests = obj("testOccurrences")?.let { BuildTests(it.text("href"), it.number("passed") ?: 0, it.number("failed") ?: 0, it.number("ignored") ?: 0, it.number("count") ?: 0, it.text("nextHref")) },
    properties = obj("properties")?.let { props ->
        BuildProperties(
            (props["property"] as? JsonArray)?.map { element ->
                val p = element.jsonObject
                BuildProperty(p.text("name"), p.text("value"), p.flag("own"), p.text("id"), p.text("href"))
            },
            props.text("id"),
            props.text("href")
        )
    },
    snapshotDependencies = obj("snapshot-dependencies")?.link(),
    personal = flag("personal"), pinned = flag("pinned"), cleanSources = flag("cleanSources"), queueAtTop = flag("queueAtTop")
)

private fun JsonObject.configuration() = BuildConfigurationData(text("id"), text("name"), text("projectId"), text("projectName"), text("href"))
private fun JsonObject.link() = BuildCollectionLink(text("href"), number("count"), text("nextHref"))
private fun JsonObject.user() = BuildUser(text("username"), text("name"), text("id"), text("href"))

data class TeamCityNodePage(val projects: List<ProjectReference>, val configurations: List<BuildConfigurationSummary>, val descriptions: Map<String, String?>)
data class TeamCityBuildPage(val builds: List<BuildLaunchData>, val nextHref: String?)
data class TeamCityServerInfo(val version: String, val webUrl: String)

/** No app state or platform dependency enters the REST contract. Each activation owns its cache. */
class TeamCityApi(private val transport: TeamCityTransport) {
    private val cacheMutex = Mutex()
    private val nodes = mutableMapOf<String, TeamCityNodePage>()
    private val initialPages = mutableMapOf<String, TeamCityBuildPage>()
    private val builds = mutableMapOf<String, BuildLaunchData>()

    suspend fun server(): TeamCityServerInfo {
        val value = transport.get(transport.apiPath("server?fields=version,webUrl"))
        return TeamCityServerInfo(value.text("version").orEmpty(), value.text("webUrl").orEmpty())
    }
    suspend fun entries(projectId: String, forceRefresh: Boolean): TeamCityNodePage {
        if (!forceRefresh) cacheMutex.withLock { nodes[projectId] }?.let { return it }
        val fields = "projects(project(id,name,description)),buildTypes(buildType(id,name,description,projectId,projectName))"
        val result = transport.get(transport.apiPath("projects/id:${projectId.encodeLocatorValue()}?fields=$fields"))
        val projects = result.obj("projects")?.items("project").orEmpty()
        val value = TeamCityNodePage(projects.map { it.toProjectReference() }, result.obj("buildTypes")?.items("buildType").orEmpty().map { it.toConfigurationSummary() }, projects.associate { requireNotNull(it.text("id")) to it.text("description") })
        cacheMutex.withLock { nodes[projectId] = value }
        return value
    }
    suspend fun page(configurationId: String, locator: String, nextHref: String?, forceRefresh: Boolean): TeamCityBuildPage {
        val key = "$configurationId:$locator"
        if (nextHref == null && !forceRefresh) cacheMutex.withLock { initialPages[key] }?.let { return it }
        val path = nextHref ?: transport.apiPath("buildTypes/id:${configurationId.encodeLocatorValue()}/builds?locator=${locator.encodeQueryValue()}&fields=${BUILD_FIELDS.encodeQueryValue()},nextHref")
        val result = transport.get(path)
        val page = TeamCityBuildPage(result.items("build").map { it.toBuildLaunchData() }, result.text("nextHref"))
        cacheMutex.withLock {
            if (nextHref == null) initialPages[key] = page
            page.builds.forEach { builds[it.href] = it }
        }
        return page
    }
    suspend fun build(href: String, forceRefresh: Boolean): BuildLaunchData {
        if (!forceRefresh) cacheMutex.withLock { builds[href] }?.let { return it }
        val path = href + (if ('?' in href) "&" else "?") + "fields=" + BUILD_FIELDS.substringAfter("build(").dropLast(1).encodeQueryValue()
        val result = transport.get(path).toBuildLaunchData()
        cacheMutex.withLock { builds[href] = result }
        return result
    }
    fun close() {
        nodes.clear()
        initialPages.clear()
        builds.clear()
        transport.close()
    }
}

private fun String.encodeQueryValue(): String = this.encodeURLQueryComponent(encodeFull = true)
private fun String.encodeLocatorValue(): String {
    require(isNotBlank() && all { it.isLetterOrDigit() || it == '_' || it == '-' || it == '.' }) { "Invalid TeamCity entity identifier" }
    return this
}

const val BUILD_FIELDS = "count,build(" +
    "id,href,webUrl,number,state,status,statusText,branchName,buildTypeId," +
    "buildType(id,name,projectId,projectName,href)," +
    "queuedDate,startDate,finishDate,waitReason,startEstimate," +
    "triggered(type,date,details,user(username,name,id,href),buildType(id,name,projectId,projectName,href))," +
    "agent(id,name,href),canceledInfo(timestamp,user(username,name,id,href))," +
    "changes(href,count,nextHref),artifacts(href)," +
    "testOccurrences(href,passed,failed,ignored,count,nextHref)," +
    "properties(id,href,property(name,value,own,id,href))," +
    "snapshot-dependencies(href,count,nextHref),personal,pinned,cleanSources,queueAtTop)"
