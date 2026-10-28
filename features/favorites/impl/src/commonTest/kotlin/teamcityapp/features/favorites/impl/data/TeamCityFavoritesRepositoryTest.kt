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

package teamcityapp.features.favorites.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.favorites.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityFavoritesRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, handler = handler) {
        lateinit var repository: TeamCityFavoritesRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityFavoritesRepository(account, api, preferences)
        }
    }

    @Test fun favoritesKeepSavedIdsWhenSomeOrEveryConfigurationFails() = runTest {
        val fixture = Fixture(this) { request ->
            if (request.url.encodedPath.contains("id:valid")) {
                respond("""{"id":"valid","name":"Build","projectId":"P","projectName":"Project"}""")
            } else {
                respond("", HttpStatusCode.NotFound)
            }
        }
        fixture.start(AccountPreferences(favoriteIds = listOf("valid", "missing")))
        val partial = fixture.repository.favorites(false)
        assertEquals(listOf("valid", "missing"), partial.savedIds)
        assertEquals(listOf("missing"), partial.unavailableIds)
        assertEquals(listOf("valid"), partial.configurations.map { it.id })
        fixture.preferences.update { it.copy(favoriteIds = it.favoriteIds - "valid") }
        val failed = fixture.repository.favorites(true)
        assertEquals(listOf("missing"), failed.savedIds)
        assertTrue(failed.configurations.isEmpty())
        assertEquals(listOf("missing"), fixture.storage.preferences(fixture.identity.key).favoriteIds)
    }
}
