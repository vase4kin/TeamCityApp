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

import kotlin.uuid.Uuid
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.*
import teamcityapp.features.artifacts.api.*
import teamcityapp.libraries.accounts.AccountScope
import teamcityapp.libraries.network.TeamCityApi

/** Platform storage owns bytes; account scope and REST guards own network lifetime. */
class TeamCityArtifactsRepository(private val account: AccountScope, private val api: TeamCityApi, private val store: ArtifactFileStore) : ArtifactsRepository {
    override suspend fun entries(url: String, forceRefresh: Boolean): List<Artifact> = account.run {
        val separator = if ('?' in url) "&" else "?"
        val document = api.document(url + separator + "locator=browseArchives:true", forceRefresh)
        (document["file"] as? JsonArray).orEmpty().map { item ->
            val file = item.jsonObject
            fun text(key: String) = (file[key] as? JsonPrimitive)?.contentOrNull
            fun href(key: String) = ((file[key] as? JsonObject)?.get("href") as? JsonPrimitive)?.contentOrNull
            Artifact(text("name").orEmpty(), text("href").orEmpty(), text("size")?.toLongOrNull() ?: 0, href("content"), href("children"))
        }
    }
    override suspend fun download(file: ArtifactDownload): DownloadedArtifact = download(file) { _, _ -> }
    override suspend fun download(file: ArtifactDownload, onProgress: (Long, Long?) -> Unit): DownloadedArtifact = account.run {
        val pending = store.create(ArtifactTarget(file.name))
        try {
            api.stream(file.href, pending::write, onProgress)
            currentCoroutineContext().ensureActive()
            pending.finish()
        } finally {
            pending.close()
        }
    }
}

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
