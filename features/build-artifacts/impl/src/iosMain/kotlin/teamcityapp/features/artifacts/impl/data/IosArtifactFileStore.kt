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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package teamcityapp.features.artifacts.impl.data

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.posix.*
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.api.DownloadedArtifact

/** Files are app-private; iOS grants per-file access through its system export sheet. */
class IosArtifactFileStore(private val directory: String) : ArtifactFileStore {
    override fun create(target: ArtifactTarget): PendingArtifact {
        val folder = "$directory/${target.transferId}"
        check(!NSFileManager.defaultManager.fileExistsAtPath(folder))
        check(NSFileManager.defaultManager.createDirectoryAtPath(folder, true, null, null)) { "Cannot create download directory" }
        val pending = "$folder/${target.pendingName}"
        val output = "$folder/${target.name}"
        val stream = checkNotNull(fopen(pending, "wb")) { "Cannot create download" }
        return object : PendingArtifact {
            var open = true
            var complete = false
            override fun write(bytes: ByteArray, count: Int) {
                check(open && !complete)
                bytes.usePinned { check(fwrite(it.addressOf(0), 1u, count.toULong(), stream) == count.toULong()) { "Cannot write download" } }
            }
            override fun finish(): DownloadedArtifact {
                check(open && !complete)
                val flushed = fflush(stream) == 0
                val closed = fclose(stream) == 0
                open = false
                check(flushed && closed) { "Cannot finish download" }
                check(rename(pending, output) == 0) { "Cannot commit download" }
                complete = true
                return DownloadedArtifact(output)
            }
            override fun close() {
                if (open) {
                    fclose(stream)
                    open = false
                }
                if (!complete) NSFileManager.defaultManager.removeItemAtPath(folder, null)
            }
        }
    }
}
