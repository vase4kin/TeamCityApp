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

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import teamcityapp.libraries.accounts.*

/** Included only with -PteamcityPlatformFixture, against scripts/kmp-platform-fixture.py. */
class PlatformEngineIntegrationTest {
    private val http = "http://127.0.0.1:18081/context/"
    private val https = "https://127.0.0.1:18444/context/"

    private fun transport(url: String, user: String = "Guest user", guest: Boolean = true, ignoreSsl: Boolean = false, consent: Boolean = false) = TeamCityTransport(platformTeamCityClient(ignoreSsl), AccountSecret(AccountIdentity(url, user, guest, ignoreSsl), "fixture-only-password"), httpConsent = consent)

    @Test fun cleartextRequiresConsentAndGuestNeverSendsPrivateCredentials() = runTest {
        val client = platformTeamCityClient(false)
        try {
            assertFailsWith<HttpConsentRequired> { TeamCityTransport(client, AccountSecret(AccountIdentity(http, "Guest user", true))) }
        } finally {
            client.close()
        }
        val accepted = transport(http, consent = true)
        try {
            assertEquals("fixture-guest", TeamCityApi(accepted).server().version)
        } finally {
            accepted.close()
        }
    }

    @Test fun privateBasicChallengesAndSameServerUsersRemainIsolated() = runTest {
        for (user in listOf("alice", "bob", "alice")) {
            val active = transport(http, user, guest = false, consent = true)
            try {
                assertEquals("fixture-$user", TeamCityApi(active).server().version)
            } finally {
                active.close()
            }
        }
    }

    @Test fun untrustedCertificateIsRejectedWithoutExplicitOverride() = runTest {
        val secure = transport(https)
        try {
            val failure = assertFails { TeamCityApi(secure).server() }
            assertFalse(failure is CancellationException)
        } finally {
            secure.close()
        }
        val accepted = transport(https, ignoreSsl = true)
        try {
            assertEquals("fixture-guest", TeamCityApi(accepted).server().version)
        } finally {
            accepted.close()
        }
    }

    @Test fun engineDoesNotFollowServerRedirects() = runTest {
        val active = transport(http, consent = true)
        try {
            val failure = assertFailsWith<TeamCityHttpFailure> { active.get(active.apiPath("server?redirect=true")) }
            assertEquals(302, failure.status)
        } finally {
            active.close()
        }
    }
}
