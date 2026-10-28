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

package teamcityapp.features.changes.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.changes.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityChangesRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, handler = handler) {
        lateinit var repository: TeamCityChangesRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityChangesRepository(account, api)
        }
    }

    @Test fun changesHydrateFilesAndPreserveDateAndStableIdentity() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.url.encodedPath.endsWith("/changes")) {
                respond("""{"count":1,"change":[{"href":"/context/app/rest/changes/id:5"}],"nextHref":"/context/app/rest/changes?cursor=next"}""")
            } else {
                respond("""{"username":"author","date":"20261021T102030+0000","comment":"Fix","version":"abc","files":{"file":[{"file":"README.md","changeType":"added"}]}}""")
            }
        }
        fixture.start()
        val page = fixture.repository.changes("/context/app/rest/changes?locator=build:(id:7)", null, false)
        assertEquals("/context/app/rest/changes/id:5", page.items.single().id)
        assertEquals("21 Oct 26 10:20", page.items.single().date)
        assertEquals("README.md", page.items.single().files.single().name)
    }
}
