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

import android.content.Context
import android.util.AtomicFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Disposable new cache namespace; never reads, overwrites or evicts installed RxCache files. */
class AndroidResponseCacheStore(context: Context) : ResponseCacheStore {
    private val file = AtomicFile(File(context.applicationContext.cacheDir, "teamcity_rest_v1.json"))
    override suspend fun read(): String? = withContext(Dispatchers.IO) {
        lock.withLock {
            if (!file.baseFile.exists()) null else file.readFully().decodeToString()
        }
    }
    override suspend fun write(value: String) = withContext(Dispatchers.IO) {
        lock.withLock {
            val stream = file.startWrite()
            try {
                stream.write(value.encodeToByteArray())
                file.finishWrite(stream)
            } catch (error: Exception) {
                file.failWrite(stream)
                throw error
            }
        }
    }
    companion object {
        private val lock = Mutex()
    }
}
