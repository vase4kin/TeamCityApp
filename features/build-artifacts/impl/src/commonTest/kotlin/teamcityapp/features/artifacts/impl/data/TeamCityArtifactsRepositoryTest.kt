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

package teamcityapp.features.artifacts.impl.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.*
import teamcityapp.features.artifacts.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

@OptIn(ExperimentalCoroutinesApi::class)
class TeamCityArtifactsRepositoryTest {
    private class Store : ArtifactFileStore {
        var opened = 0
        var closed = 0
        var finished = 0
        var bytes = 0
        var largestChunk = 0
        var failWrite = false
        override fun create(target: teamcityapp.features.artifacts.api.ArtifactTarget): PendingArtifact {
            opened++
            return object : PendingArtifact {
                override fun write(bytes: ByteArray, count: Int) {
                    if (failWrite) error("disk full")
                    this@Store.bytes += count
                    largestChunk = maxOf(largestChunk, count)
                }
                override fun finish(): DownloadedArtifact {
                    finished++
                    return DownloadedArtifact("/private/${target.name}")
                }
                override fun close() {
                    closed++
                }
            }
        }
    }
    private class Vault : SecureVault {
        var value: String? = null
        override suspend fun read() = value
        override suspend fun write(value: String) {
            this.value = value
        }
    }
    private class Fixture(val scope: TestScope, handler: MockRequestHandler) {
        val store = Store()
        val sessions = AccountSessionManager(VaultAccountStorage(Vault()), scope.backgroundScope.coroutineContext)
        val identity = AccountIdentity("https://teamcity.example/context/", "Guest user", true)
        val api = TeamCityApi(
            TeamCityTransport(
                HttpClient(
                    MockEngine(
                        MockEngineConfig().apply {
                            dispatcher = Dispatchers.Unconfined
                            addHandler(handler)
                        }
                    )
                ) {
                    followRedirects = false
                    install(HttpTimeout)
                },
                AccountSecret(identity, "")
            )
        )
        lateinit var repository: TeamCityArtifactsRepository
        suspend fun start() {
            sessions.saveAndActivate(AccountSecret(identity, ""))
            repository = TeamCityArtifactsRepository((sessions.state.value as AccountSessionState.Active).scope, api, store)
        }
        fun close() {
            api.close()
            sessions.close()
        }
    }
    private val file = ArtifactDownload("report.txt", "/context/app/rest/builds/id:1/artifacts/content/report.txt")

    @Test fun archiveMetadataKeepsOpaqueLinksAndInstalledSizeDistinction() = runTest {
        val paths = mutableListOf<String>()
        val fixture = Fixture(this) { request ->
            paths += request.url.toString()
            respond("""{"file":[{"name":"bundle.zip","href":"opaque","size":1024,"content":{"href":"download"},"children":{"href":"archive!/children"}},{"name":"empty.txt","size":0}]}""")
        }
        try {
            fixture.start()
            val rows = fixture.repository.entries("/context/app/rest/builds/id:1/artifacts/children?x=1", false)
            assertEquals("archive!/children", rows[0].childrenHref)
            assertFalse(rows[0].isFolder)
            assertTrue(rows[1].isFolder)
            assertTrue(paths.single().contains("x=1&locator=browseArchives:true"))
            fixture.repository.entries("/context/app/rest/builds/id:1/artifacts/children?x=1", false)
            assertEquals(1, paths.size)
        } finally {
            fixture.close()
        }
    }

    @Test fun largeDownloadUsesBoundedChunksReportsProgressAndFinishesOnce() = runTest {
        val payload = "x".repeat(200_000)
        val fixture = Fixture(this) { respond(payload, headers = headersOf(HttpHeaders.ContentLength, payload.length.toString())) }
        try {
            fixture.start()
            val progress = mutableListOf<Pair<Long, Long?>>()
            assertEquals("/private/report.txt", fixture.repository.download(file) { count, total -> progress += count to total }.path)
            assertEquals(200_000, fixture.store.bytes)
            assertTrue(fixture.store.largestChunk <= 8192)
            assertEquals(200_000L to 200_000L, progress.last())
            assertEquals(1, fixture.store.finished)
            assertEquals(1, fixture.store.closed)
        } finally {
            fixture.close()
        }
    }

    @Test fun truncatedContentNeverCommitsAndClosesThePendingFile() = runTest {
        val fixture = Fixture(this) { respond("short", headers = headersOf(HttpHeaders.ContentLength, "100")) }
        try {
            fixture.start()
            assertFails { fixture.repository.download(file) }
            assertEquals(0, fixture.store.finished)
            assertEquals(1, fixture.store.closed)
        } finally {
            fixture.close()
        }
    }

    @Test fun writeFailureAndForbiddenResponseRemovePendingBytesWithoutFinishing() = runTest {
        for (forbidden in listOf(false, true)) {
            val fixture = Fixture(this) { respond("content", if (forbidden) HttpStatusCode.Forbidden else HttpStatusCode.OK) }
            try {
                fixture.start()
                fixture.store.failWrite = !forbidden
                assertFails { fixture.repository.download(file) }
                assertEquals(0, fixture.store.finished)
                assertEquals(1, fixture.store.closed)
            } finally {
                fixture.close()
            }
        }
    }

    @Test fun cancellationClosesABlockedStreamAndDoesNotCommit() = runTest {
        val body = ByteChannel()
        val entered = CompletableDeferred<Unit>()
        val fixture = Fixture(this) {
            entered.complete(Unit)
            respond(body, HttpStatusCode.OK)
        }
        try {
            fixture.start()
            val task = launch { fixture.repository.download(file) }
            entered.await()
            runCurrent()
            task.cancelAndJoin()
            assertTrue(task.isCancelled)
            assertEquals(0, fixture.store.finished)
            assertEquals(1, fixture.store.closed)
        } finally {
            body.cancel()
            fixture.close()
        }
    }

    @Test fun accountReplacementCancelsTheOldTransfer() = runTest {
        val body = ByteChannel()
        val entered = CompletableDeferred<Unit>()
        val fixture = Fixture(this) {
            entered.complete(Unit)
            respond(body, HttpStatusCode.OK)
        }
        try {
            fixture.start()
            val task = async { fixture.repository.download(file) }
            entered.await()
            runCurrent()
            fixture.sessions.saveAndActivate(AccountSecret(fixture.identity.copy(userName = "bob", guest = false), "fixture"))
            assertFailsWith<CancellationException> { task.await() }
            assertEquals(0, fixture.store.finished)
            assertEquals(1, fixture.store.closed)
        } finally {
            body.cancel()
            fixture.close()
        }
    }

    @Test fun malformedNamesAndCrossOriginPathsCannotCommit() = runTest {
        var requests = 0
        val fixture = Fixture(this) {
            requests++
            respond("content")
        }
        try {
            fixture.start()
            for (name in listOf("", ".", "..", "../file", "a/b", "a\\b", "a\u0000b")) assertFails { fixture.repository.download(file.copy(name = name)) }
            assertEquals(0, fixture.store.opened)
            assertFails { fixture.repository.download(file.copy(href = "https://elsewhere.example/file")) }
            assertEquals(0, requests)
            assertEquals(0, fixture.store.finished)
            assertEquals(1, fixture.store.closed)
        } finally {
            fixture.close()
        }
    }
}
