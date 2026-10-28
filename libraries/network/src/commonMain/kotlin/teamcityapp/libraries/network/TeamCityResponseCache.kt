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

import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

/** Native code owns atomic byte storage only; format, account guards, expiry and bounds are common. */
interface ResponseCacheStore {
    suspend fun read(): String?
    suspend fun write(value: String)
}

class TeamCityResponseCache(
    private val accountKey: String,
    private val store: ResponseCacheStore? = null,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    private data class Entry(val saved: Long, val lifetime: Long, val value: JsonObject)
    private val lock = Mutex()
    private val values = linkedMapOf<String, Entry>()
    private var loaded = false
    private var revision = 0L

    suspend fun get(path: String, force: Boolean, fetch: suspend () -> JsonObject): JsonObject {
        var version = 0L
        val cached = lock.withLock {
            restore()
            version = revision
            values[path]?.takeIf { !force && now() >= it.saved && now() - it.saved < it.lifetime }?.value
        }
        if (cached != null) return cached
        val result = fetch()
        lock.withLock {
            if (revision == version) {
                values.remove(path)
                if (result.toString().length <= MAX_DOCUMENT_CHARS) values[path] = Entry(now(), lifetime(path, result), result)
                while (values.size > MAX_ENTRIES || values.values.sumOf { it.value.toString().length } > MAX_TOTAL_CHARS) values.remove(values.keys.first())
                persist()
            }
        }
        return result
    }

    suspend fun invalidate() = lock.withLock {
        loaded = true
        revision++
        values.clear()
        persist()
    }

    private suspend fun restore() {
        if (loaded) return
        loaded = true
        optionalStorage {
            val raw = store?.read() ?: return@optionalStorage
            if (raw.length > MAX_TOTAL_CHARS * 2) return@optionalStorage
            val root = Json.parseToJsonElement(raw).jsonObject
            if (root["schema"]?.jsonPrimitive?.int != 1 || root["account"]?.jsonPrimitive?.content != accountKey) return@optionalStorage
            root["entries"]?.jsonArray?.takeLast(MAX_ENTRIES)?.forEach { element ->
                val entry = element.jsonObject
                val value = entry.getValue("value").jsonObject
                if (value.toString().length <= MAX_DOCUMENT_CHARS) values[entry.getValue("path").jsonPrimitive.content] = Entry(entry.getValue("saved").jsonPrimitive.long, entry.getValue("lifetime").jsonPrimitive.long.coerceIn(0, DAY), value)
            }
        }
    }

    private suspend fun persist() = optionalStorage {
        val encoded = buildJsonObject {
            put("schema", 1)
            put("account", accountKey)
            put(
                "entries",
                buildJsonArray {
                    values.forEach { (path, entry) ->
                        add(
                            buildJsonObject {
                                put("path", path)
                                put("saved", entry.saved)
                                put("lifetime", entry.lifetime)
                                put("value", entry.value)
                            }
                        )
                    }
                }
            )
        }
        store?.write(encoded.toString())
    }

    private suspend fun optionalStorage(block: suspend () -> Unit) {
        try {
            block()
        } catch (c: CancellationException) {
            throw c
        } catch (_: Exception) {
            // A disposable cache failure must not become an authentication or mutation failure.
        }
    }

    companion object {
        private const val MINUTE = 60_000L
        private const val HOUR = 60 * MINUTE
        private const val DAY = 24 * HOUR
        private const val MAX_ENTRIES = 128
        private const val MAX_DOCUMENT_CHARS = 1_000_000
        private const val MAX_TOTAL_CHARS = 4_000_000
        internal fun lifetime(path: String, value: JsonObject): Long = when {
            value["state"]?.jsonPrimitive?.content in listOf("running", "queued") -> 30_000L
            value["id"] != null && value["href"]?.jsonPrimitive?.content?.contains("/builds/") == true -> HOUR
            "/projects/" in path || ("/buildTypes/" in path && "/builds" !in path && "/branches" !in path) -> DAY
            "/testOccurrences/" in path -> HOUR
            "/changes/" in path -> MINUTE
            "/agents" in path || "/changes" in path || ("/testOccurrences" in path && value["testOccurrence"] != null) -> 3 * MINUTE
            "/server" in path || "/artifacts" in path -> HOUR
            else -> MINUTE
        }
    }
}
