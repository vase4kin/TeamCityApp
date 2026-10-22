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

import java.io.File
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import teamcityapp.libraries.accounts.AccountIdentity
import teamcityapp.libraries.accounts.AccountSecret
import teamcityapp.libraries.build_configurations.ProjectReference

/** Opt-in read-only verification; normal CI uses deterministic engine and repository fixtures. */
class TeamCityLiveSmokeTest {
    @Test fun guestJourneyReadsARealServerAndACompleteBuild() = runBlocking {
        val server = System.getProperty("teamcity.liveServer")
        assumeTrue("Live verification requires -PteamcityLiveServer", server != null)
        val api = TeamCityApi(TeamCityTransport(platformTeamCityClient(false), AccountSecret(AccountIdentity(checkNotNull(server), "Guest user", true))))
        try {
            withTimeout(60_000) {
                val info = api.server()
                assertTrue(info.version.isNotBlank())
                val queue = ArrayDeque<Pair<String, List<ProjectReference>>>()
                queue.add("_Root" to emptyList())
                var inspected = 0
                while (queue.isNotEmpty() && inspected++ < 30) {
                    val (id, path) = queue.removeFirst()
                    val nodes = api.entries(id, false)
                    for (configuration in nodes.configurations.take(3)) {
                        val history = api.page(configuration.id, "state:any,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10", null, false)
                        val snapshot = history.builds.firstOrNull() ?: continue
                        val loaded = api.build(snapshot.href, true)
                        assertEquals(snapshot.id, loaded.id)
                        assertNotNull(loaded.configuration)
                        assertTrue(loaded.configuration!!.id.orEmpty().isNotBlank())
                        val next = history.nextHref?.let { api.page(configuration.id, "unused:opaque", it, false) }
                        val evidence = buildJsonObject {
                            put("server", server)
                            put("serverVersion", info.version)
                            put(
                                "projects",
                                buildJsonArray {
                                    path.forEach { project ->
                                        add(
                                            buildJsonObject {
                                                put("id", project.id)
                                                put("name", project.name)
                                            }
                                        )
                                    }
                                }
                            )
                            put("configurationId", configuration.id)
                            put("configurationName", configuration.name)
                            put("buildId", loaded.id)
                            put("buildHref", loaded.href)
                            put("firstPageCount", history.builds.size)
                            put("continuationVerified", next != null)
                            put("secondPageCount", next?.builds?.size ?: 0)
                        }
                        System.getProperty("teamcity.liveEvidence")?.let {
                            File(it).apply {
                                parentFile?.mkdirs()
                                writeText(evidence.toString())
                            }
                        }
                        return@withTimeout
                    }
                    nodes.projects.take(20).forEach { queue.add(it.id to (path + it)) }
                }
                fail("No accessible configuration with builds found in the bounded public-server traversal")
            }
        } finally {
            api.close()
        }
    }
}
