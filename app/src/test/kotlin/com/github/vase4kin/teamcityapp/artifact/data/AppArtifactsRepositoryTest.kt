/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.artifact.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.api.File as ArtifactFile
import com.github.vase4kin.teamcityapp.artifact.api.Files
import io.reactivex.Single
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.*
import teamcityapp.features.artifacts.api.ArtifactDownload

@OptIn(ExperimentalCoroutinesApi::class)
class AppArtifactsRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun adapter(repository: Repository, dispatcher: CoroutineDispatcher, directory: java.io.File = temporary.root) = AppArtifactsRepository(Provider { repository }, dispatcher) { directory }

    @Test fun entriesKeepServerOrderArchiveMetadataAndForcedCachePolicy() = runTest {
        val repository = mock<Repository>()
        val folder = ArtifactFile("folder", ArtifactFile.Children("opaque/folder&x=1"), "metadata/folder")
        val archive = mock<ArtifactFile> {
            on { name } doReturn "bundle.zip"
            on { href } doReturn "metadata/bundle.zip"
            on { size } doReturn 1024L
            on { content } doReturn ArtifactFile.Content("opaque/download.zip")
            on { children } doReturn ArtifactFile.Children("opaque/archive!/children")
        }
        doReturn(Single.just(Files(listOf(folder, archive)))).whenever(repository).listArtifacts("opaque/root&start=0", "browseArchives:true", false)
        doReturn(Single.just(Files(listOf(archive)))).whenever(repository).listArtifacts("opaque/root&start=0", "browseArchives:true", true)
        val adapter = adapter(repository, StandardTestDispatcher(testScheduler))
        val initial = adapter.entries("opaque/root&start=0", false)
        assertEquals(listOf("folder", "bundle.zip"), initial.map { it.name })
        assertTrue(initial.first().isFolder)
        assertEquals("opaque/folder&x=1", initial.first().childrenHref)
        assertNull(initial.first().contentHref)
        assertEquals(1024L, initial.last().size)
        assertEquals("opaque/download.zip", initial.last().contentHref)
        assertEquals("opaque/archive!/children", initial.last().childrenHref)
        assertEquals("metadata/bundle.zip", initial.last().href)
        assertEquals(listOf("bundle.zip"), adapter.entries("opaque/root&start=0", true).map { it.name })
        verify(repository).listArtifacts("opaque/root&start=0", "browseArchives:true", true)
    }

    @Test fun entriesCopyTheReturnedListAndTreatAbsentFileCollectionAsEmpty() = runTest {
        val repository = mock<Repository>()
        val source = mutableListOf(ArtifactFile("zero-byte.txt", 0, ArtifactFile.Content("download/zero"), "metadata/zero"))
        doReturn(Single.just(Files(source))).whenever(repository).listArtifacts("root", "browseArchives:true", false)
        val adapter = adapter(repository, StandardTestDispatcher(testScheduler))
        val rows = adapter.entries("root", false)
        source.clear()
        assertEquals(1, rows.size)
        assertTrue(rows.single().isFolder)
        doReturn(Single.just(Files(null))).whenever(repository).listArtifacts("root", "browseArchives:true", false)
        assertTrue(adapter.entries("root", false).isEmpty())
    }

    @Test fun eachOperationResolvesOneCurrentAccountSession() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        doReturn(Single.just(Files(emptyList()))).whenever(first).listArtifacts("root", "browseArchives:true", false)
        doReturn(Single.just("new-account".toResponseBody())).whenever(second).downloadFile("opaque/file")
        var current = first
        var resolutions = 0
        val adapter = AppArtifactsRepository(
            Provider {
                resolutions++
                current
            },
            StandardTestDispatcher(testScheduler)
        ) { temporary.root }
        adapter.entries("root", false)
        current = second
        assertEquals("new-account", java.io.File(adapter.download(ArtifactDownload("file.txt", "opaque/file")).path).readText())
        assertEquals(2, resolutions)
        verify(first, never()).downloadFile(any())
        verify(second, never()).listArtifacts(any(), any(), any())
    }

    @Test fun pendingEntriesCancellationDisposesTheRxRequest() = runTest {
        val repository = mock<Repository>()
        var disposed = false
        doReturn(Single.never<Files>().doOnDispose { disposed = true }).whenever(repository).listArtifacts("root", "browseArchives:true", false)
        val pending = async { adapter(repository, StandardTestDispatcher(testScheduler)).entries("root", false) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
        assertTrue(pending.isCancelled)
    }

    @Test fun pendingDownloadCancellationDisposesRxAndCreatesNoFiles() = runTest {
        val repository = mock<Repository>()
        var disposed = false
        doReturn(Single.never<ResponseBody>().doOnDispose { disposed = true }).whenever(repository).downloadFile("opaque/file")
        val pending = async { adapter(repository, StandardTestDispatcher(testScheduler)).download(ArtifactDownload("file.txt", "opaque/file")) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
        assertTrue(temporary.root.listFiles().orEmpty().isEmpty())
    }

    @Test fun successfulDownloadUsesTheOriginalFilenameAndReplacesCompletedContents() = runTest {
        val repository = mock<Repository>()
        val target = java.io.File(temporary.root, "report.zip").apply { writeText("old report") }
        val body = TrackingBody(Buffer().writeUtf8("new report"))
        doReturn(Single.just<ResponseBody>(body)).whenever(repository).downloadFile("opaque/archive!/download")
        val result = adapter(repository, StandardTestDispatcher(testScheduler)).download(ArtifactDownload("report.zip", "opaque/archive!/download"))
        assertEquals(target.absolutePath, result.path)
        assertEquals("new report", target.readText())
        assertTrue(body.closed.get())
        assertEquals(listOf("report.zip"), temporary.root.listFiles().orEmpty().map { it.name })
        verify(repository).downloadFile("opaque/archive!/download")
    }

    @Test fun missingDownloadDirectoryIsCreatedBeforeStreaming() = runTest {
        val repository = mock<Repository>()
        val directory = java.io.File(temporary.root, "Downloads")
        doReturn(Single.just("apk".toResponseBody())).whenever(repository).downloadFile("download/apk")
        val result = adapter(repository, StandardTestDispatcher(testScheduler), directory).download(ArtifactDownload("app.apk", "download/apk"))
        assertTrue(directory.isDirectory)
        assertEquals("apk", java.io.File(result.path).readText())
    }

    @Test fun networkFailureLeavesAnExistingDownloadedFileIntact() = runTest {
        val repository = mock<Repository>()
        val original = java.io.File(temporary.root, "file.txt").apply { writeText("completed") }
        doReturn(Single.error<ResponseBody>(IOException("offline"))).whenever(repository).downloadFile("download/file")
        val failure = runCatching { adapter(repository, StandardTestDispatcher(testScheduler)).download(ArtifactDownload("file.txt", "download/file")) }.exceptionOrNull()
        assertEquals("offline", failure?.message)
        assertEquals("completed", original.readText())
        assertEquals(1, temporary.root.listFiles().orEmpty().size)
    }

    @Test fun streamFailureClosesTheBodyAndDeletesPartialDataWithoutOverwritingOldFile() = runTest {
        val repository = mock<Repository>()
        val original = java.io.File(temporary.root, "file.txt").apply { writeText("completed") }
        val body = TrackingBody(object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long = throw IOException("broken stream")
            override fun timeout(): Timeout = Timeout.NONE
            override fun close() = Unit
        })
        doReturn(Single.just<ResponseBody>(body)).whenever(repository).downloadFile("download/file")
        val failure = runCatching { adapter(repository, StandardTestDispatcher(testScheduler)).download(ArtifactDownload("file.txt", "download/file")) }.exceptionOrNull()
        assertEquals("broken stream", failure?.message)
        assertTrue(body.closed.get())
        assertEquals("completed", original.readText())
        assertEquals(listOf("file.txt"), temporary.root.listFiles().orEmpty().map { it.name })
    }

    @Test fun cancellationWhileReadingClosesTheNetworkBodyAndKeepsCompletedFile() = runTest {
        val repository = mock<Repository>()
        val original = java.io.File(temporary.root, "file.txt").apply { writeText("completed") }
        val entered = CountDownLatch(1)
        val closed = CountDownLatch(1)
        val body = TrackingBody(object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long {
                entered.countDown()
                check(closed.await(5, TimeUnit.SECONDS)) { "Cancellation did not close the network source" }
                throw IOException("closed")
            }
            override fun timeout(): Timeout = Timeout.NONE
            override fun close() {
                closed.countDown()
            }
        })
        doReturn(Single.just<ResponseBody>(body)).whenever(repository).downloadFile("download/file")
        Executors.newFixedThreadPool(2).asCoroutineDispatcher().use { io ->
            val pending = async { adapter(repository, io).download(ArtifactDownload("file.txt", "download/file")) }
            runCurrent()
            assertTrue(withContext(Dispatchers.IO) { entered.await(5, TimeUnit.SECONDS) })
            pending.cancelAndJoin()
            val failure = runCatching { pending.await() }.exceptionOrNull()
            assertTrue("The closed read must remain cancellation: $failure", failure is CancellationException)
            assertTrue(pending.isCancelled)
            assertTrue(body.closed.get())
            assertEquals("completed", original.readText())
            assertEquals(listOf("file.txt"), temporary.root.listFiles().orEmpty().map { it.name })
        }
    }

    @Test fun failureToFinishDownloadClosesTheBodyAndRemovesTheTemporaryFile() = runTest {
        val repository = mock<Repository>()
        val target = java.io.File(temporary.root, "folder").apply { mkdirs() }
        java.io.File(target, "keep.txt").writeText("keep")
        val body = TrackingBody(Buffer().writeUtf8("new content"))
        doReturn(Single.just<ResponseBody>(body)).whenever(repository).downloadFile("download/file")
        val failure = runCatching { adapter(repository, StandardTestDispatcher(testScheduler)).download(ArtifactDownload("folder", "download/file")) }.exceptionOrNull()
        assertTrue(failure is IOException)
        assertTrue(body.closed.get())
        assertEquals("keep", java.io.File(target, "keep.txt").readText())
        assertEquals(listOf("folder"), temporary.root.listFiles().orEmpty().map { it.name })
    }

    private class TrackingBody(source: Source) : ResponseBody() {
        val closed = AtomicBoolean()
        private val buffered = object : ForwardingSource(source) {
            override fun close() {
                closed.set(true)
                super.close()
            }
        }.buffer()
        override fun contentType(): MediaType? = null
        override fun contentLength(): Long = -1
        override fun source(): BufferedSource = buffered
    }
}
