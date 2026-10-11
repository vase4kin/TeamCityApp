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

import android.content.Context
import android.os.Environment
import com.github.vase4kin.teamcityapp.api.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.buffer
import okio.sink
import teamcityapp.features.artifacts.api.Artifact
import teamcityapp.features.artifacts.api.ArtifactDownload
import teamcityapp.features.artifacts.api.ArtifactsRepository
import teamcityapp.features.artifacts.api.DownloadedArtifact
import teamcityapp.libraries.coroutines.IoDispatcher

/** Keeps RxCache keys, opaque archive URLs, and application-private downloads behind suspend APIs. */
class AppArtifactsRepository internal constructor(
    private val repository: Provider<Repository>,
    private val ioDispatcher: CoroutineDispatcher,
    private val downloadDirectory: () -> File
) : ArtifactsRepository {
    @Inject constructor(
        repository: Provider<Repository>,
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : this(repository, ioDispatcher, { context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir })

    override suspend fun entries(url: String, forceRefresh: Boolean): List<Artifact> = withContext(ioDispatcher) {
        val session = repository.get()
        val files = session.listArtifacts(url, "browseArchives:true", forceRefresh).await()
        currentCoroutineContext().ensureActive()
        files.objects.orEmpty().map { file ->
            Artifact(file.name.orEmpty(), file.href.orEmpty(), file.size, file.content?.href, file.children?.href)
        }
    }

    override suspend fun download(file: ArtifactDownload): DownloadedArtifact = withContext(ioDispatcher) {
        val session = repository.get()
        val response = session.downloadFile(file.href).await()
        var temporary: File? = null
        try {
            currentCoroutineContext().ensureActive()
            val directory = downloadDirectory()
            if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Could not create artifact download directory")
            val output = File(directory, file.name)
            val pendingFile = File.createTempFile("artifact-", ".part", directory)
            temporary = pendingFile
            coroutineScope {
                val copy = async(ioDispatcher) {
                    try {
                        pendingFile.sink().buffer().use { sink ->
                            val source = response.source()
                            val buffer = Buffer()
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = source.read(buffer, 8192)
                                if (count == -1L) break
                                currentCoroutineContext().ensureActive()
                                sink.write(buffer, count)
                            }
                        }
                    } catch (failure: Exception) {
                        // Closing a cancelled download can interrupt a blocking read with IOException.
                        // Preserve cancellation before the child can promote that I/O failure to its parent.
                        currentCoroutineContext().ensureActive()
                        throw failure
                    }
                }
                try {
                    copy.await()
                } finally {
                    // Closing the body also unblocks a pending network read during explicit cancellation.
                    response.close()
                }
            }
            currentCoroutineContext().ensureActive()
            // Same-directory rename is supported on API 24 and keeps a completed old file on failure/cancellation.
            if (!pendingFile.renameTo(output)) throw IOException("Could not finish artifact download")
            DownloadedArtifact(output.absolutePath)
        } finally {
            response.close()
            temporary?.delete()
        }
    }
}
