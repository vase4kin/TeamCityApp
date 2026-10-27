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

package teamcityapp.features.navigation.impl.data

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.navigation.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.network.testing.AccountNetworkFixture

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityNavigationRatingRepositoryTest {
    private class Fixture(scope: TestScope, handler: MockRequestHandler) : AccountNetworkFixture(scope, handler = handler) {
        lateinit var repository: TeamCityNavigationRatingRepository
        override suspend fun start(initial: AccountPreferences) {
            super.start(initial)
            repository = TeamCityNavigationRatingRepository(account, preferences, "launch-one")
        }
    }

    @Test fun ratingRequiresThreeDistinctSuccessfulViewsAndALaterAppLaunch() = runTest {
        val fixture = Fixture(this) { error("Local rating policy must not make requests") }
        fixture.start()
        fun repositories(initial: AccountPreferences, launch: String) = TeamCityNavigationRatingRepository(fixture.account, AccountPreferenceState(fixture.account, initial) { fixture.sessions.updatePreferences(fixture.account, it) }, launch)
        for (id in listOf("1", "1", "2")) fixture.repository.buildViewed(id)
        assertEquals(setOf("1", "2"), fixture.sessions.preferences(fixture.account).ratingViewedBuildIds)
        assertFalse(fixture.repository.isEligible())
        fixture.repository.buildViewed("3")
        fixture.repository.buildViewed("4")
        val qualified = fixture.sessions.preferences(fixture.account)
        assertEquals(setOf("1", "2", "3"), qualified.ratingViewedBuildIds)
        assertEquals("launch-one", qualified.ratingQualifiedLaunch)
        assertFalse(fixture.repository.isEligible())
        assertFalse(repositories(qualified, "launch-one").isEligible()) // account/host recreation is not another launch
        val later = repositories(qualified, "launch-two")
        assertTrue(later.isEligible())
        later.markHandled()
        assertFalse(later.isEligible())
        assertFalse(repositories(fixture.sessions.preferences(fixture.account), "launch-three").isEligible())
        assertTrue(fixture.urls.isEmpty())
        fixture.sessions.signOut()
        assertFailsWith<CancellationException> { fixture.repository.buildViewed("1") }
    }

    @Test fun ratingMilestoneWritesAreRetryableAndCannotCrossAccounts() = runTest {
        val fixture = Fixture(this) { error("No request") }
        fixture.start()
        fixture.repository.buildViewed("1")
        fixture.vault.fail = true
        assertFails { fixture.repository.buildViewed("2") }
        assertEquals(setOf("1"), fixture.sessions.preferences(fixture.account).ratingViewedBuildIds)
        fixture.vault.fail = false
        fixture.repository.buildViewed("2")
        assertEquals(setOf("1", "2"), fixture.sessions.preferences(fixture.account).ratingViewedBuildIds)
        val bob = fixture.identity.copy(userName = "bob")
        fixture.sessions.saveAndActivate(AccountSecret(bob))
        val active = (fixture.sessions.state.value as AccountSessionState.Active).scope
        assertEquals(AccountPreferences(), fixture.sessions.preferences(active))
        assertFailsWith<CancellationException> { fixture.repository.buildViewed("3") }
        assertEquals(AccountPreferences(), fixture.sessions.preferences(active))
        val stored = fixture.storage.preferences(fixture.identity.key)
        assertEquals(setOf("1", "2"), stored.ratingViewedBuildIds)
        assertNull(stored.ratingQualifiedLaunch)
    }
}
