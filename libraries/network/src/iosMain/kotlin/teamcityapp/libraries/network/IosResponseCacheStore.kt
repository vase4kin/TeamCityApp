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

package teamcityapp.libraries.network

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.Foundation.*

/** Foundation owns atomic file bytes; account partitioning and cache lifetime are common. */
class IosResponseCacheStore : ResponseCacheStore {
    private val path = NSHomeDirectory() + "/Library/Caches/teamcity_rest_v1.json"
    override suspend fun read(): String? = withContext(Dispatchers.Default) {
        lock.withLock {
            if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
                null
            } else {
                checkNotNull(NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null))
            }
        }
    }
    override suspend fun write(value: String) = withContext(Dispatchers.Default) {
        lock.withLock {
            val bytes = value.encodeToByteArray()
            bytes.usePinned {
                val data = NSData.dataWithBytes(if (bytes.isEmpty()) null else it.addressOf(0), bytes.size.toULong())
                check(data.writeToFile(path, true)) { "Cannot persist response cache" }
            }
        }
    }
    companion object {
        private val lock = Mutex()
    }
}
