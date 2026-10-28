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

package teamcityapp.features.artifacts.api

import kotlin.uuid.Uuid

/** Common naming and no-clobber policy: every transfer owns a unique directory. */
class ArtifactTarget(val name: String) {
    val transferId: String = Uuid.random().toString()
    val pendingName: String = ".download.part"
    init {
        require(name.isNotBlank() && name !in setOf(".", "..", pendingName) && name.none { it == '/' || it == '\\' || it.code < 32 }) { "Invalid artifact filename" }
    }
}
interface ArtifactFileStore {
    fun create(target: ArtifactTarget): PendingArtifact
    fun create(name: String): PendingArtifact = create(ArtifactTarget(name))
}
interface PendingArtifact {
    fun write(bytes: ByteArray, count: Int)
    fun finish(): DownloadedArtifact

    /** Must remove partial bytes, retaining a successfully finished file. */
    fun close()
}
