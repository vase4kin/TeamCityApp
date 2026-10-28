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

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import teamcityapp.libraries.accounts.AccountIdentity
import teamcityapp.libraries.accounts.AccountSecret

class TeamCityApiTest {
    @Test fun completeBuildSnapshotPreservesWireFieldsAndNumericIdentifiers() {
        val raw = Json.parseToJsonElement(
            """
            {"id":42,"href":"/app/rest/builds/id:42","webUrl":"https://server/viewLog.html?buildId=42","number":"1.2","state":"finished","status":"FAILURE","statusText":"Tests failed","branchName":"feature/a","buildTypeId":"Build_A","buildType":{"id":"Build_A","name":"Android","projectId":"P","projectName":"Project","href":"/app/rest/buildTypes/id:Build_A"},"queuedDate":"q","startDate":"s","finishDate":"f","waitReason":"wait","startEstimate":"e","triggered":{"type":"user","date":"t","details":"Manual","user":{"username":"alice","name":"Alice","id":7,"href":"user"},"buildType":{"id":"Other"}},"agent":{"id":9,"name":"macOS","href":"agent"},"canceledInfo":{"timestamp":"c","user":{"username":"bob"}},"changes":{"href":"changes","count":2,"nextHref":"opaque"},"artifacts":{"href":"artifacts"},"testOccurrences":{"href":"tests","passed":3,"failed":1,"ignored":2,"count":6,"nextHref":"next-tests"},"properties":{"id":"props","href":"properties","property":[{"name":"env.NAME","value":"value","own":true,"id":"prop","href":"p"}]},"snapshot-dependencies":{"href":"deps","count":0},"personal":true,"pinned":true,"cleanSources":true,"queueAtTop":true,"futureField":"ignored"}
        """
        ).jsonObject
        val build = raw.toBuildLaunchData()
        assertEquals("42", build.id)
        assertEquals("Project", build.configuration?.projectName)
        assertEquals("feature/a", build.branchName)
        assertEquals("7", build.triggered?.user?.id)
        assertEquals("Other", build.triggered?.configuration?.id)
        assertEquals("9", build.agent?.id)
        assertEquals("bob", build.canceledInfo?.user?.username)
        assertEquals("opaque", build.changes?.nextHref)
        assertNull(build.artifacts?.count)
        assertEquals(6, build.tests?.count)
        assertEquals("next-tests", build.tests?.nextHref)
        assertEquals("value", build.properties?.items?.single()?.value)
        assertTrue(build.properties!!.items!!.single().own)
        assertEquals(0, build.snapshotDependencies?.count)
        assertTrue(build.personal && build.pinned && build.cleanSources && build.queueAtTop)
    }

    @Test fun optionalWrappersPreserveAbsentNullAndEmptyCollections() {
        fun build(extra: String) = Json.parseToJsonElement("""{"id":"1","href":"build"$extra}""").jsonObject.toBuildLaunchData()
        assertNull(build("").properties)
        assertNull(build(",\"properties\":{}").properties?.items)
        assertEquals(emptyList(), build(",\"properties\":{\"property\":[]}").properties?.items)
        assertFails { Json.parseToJsonElement("{} ").jsonObject.toBuildLaunchData() }
    }

    @Test fun firstPageCacheRefreshAndOpaqueContinuationHaveDifferentSemantics() = runTest {
        val urls = mutableListOf<String>()
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                addHandler { request ->
                    urls += request.url.toString()
                    respond("""{"count":1,"build":[{"id":1,"href":"/app/rest/builds/id:1"}],"nextHref":"/app/rest/builds?locator=opaque%3Avalue&x=a+b"}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
            }
        )
        val api = TeamCityApi(TeamCityTransport(HttpClient(engine) { followRedirects = false }, AccountSecret(AccountIdentity("https://teamcity.example", "Guest user", true))))
        try {
            val first = api.page("Build_A", "count:10,state:any", null, false)
            api.page("Build_A", "count:10,state:any", null, false)
            assertEquals(1, urls.size)
            api.page("Build_A", "count:10,state:any", null, true)
            assertEquals(2, urls.size)
            api.page("Build_A", "count:10,state:any", first.nextHref, false)
            api.page("Build_A", "count:10,state:any", first.nextHref, false)
            assertEquals(4, urls.size)
            assertTrue(urls.last().endsWith("/guestAuth/app/rest/builds?locator=opaque%3Avalue&x=a+b"))
        } finally {
            api.close()
        }
    }
}
