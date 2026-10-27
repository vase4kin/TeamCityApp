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

package teamcityapp.libraries.shared_data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import teamcityapp.features.artifacts.api.DownloadedArtifact

/** The common transfer target owns naming; this adapter only writes and atomically moves bytes. */
class AndroidArtifactFileStore(context: Context) : ArtifactFileStore {
    private val context = context.applicationContext
    override fun create(target: ArtifactTarget): PendingArtifact {
        val folder = File(context.cacheDir, "TeamCityArtifacts/${target.transferId}")
        check(!folder.exists() && folder.mkdirs()) { "Cannot create download directory" }
        val pending = File(folder, target.pendingName)
        val output = File(folder, target.name)
        val stream = FileOutputStream(pending)
        return object : PendingArtifact {
            private var open = true
            private var complete = false
            override fun write(bytes: ByteArray, count: Int) {
                check(open)
                stream.write(bytes, 0, count)
            }
            override fun finish(): DownloadedArtifact {
                check(open)
                stream.fd.sync()
                stream.close()
                open = false
                check(pending.renameTo(output)) { "Cannot finish artifact download" }
                complete = true
                return DownloadedArtifact(output.absolutePath)
            }
            override fun close() {
                if (open) {
                    stream.close()
                    open = false
                }
                if (!complete) folder.deleteRecursively()
            }
        }
    }
}
