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

import android.app.Application
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AndroidResponseCacheStoreTest {
    @Test fun cacheSurvivesStoreRecreationAndKeepsLegacyCacheFilesUntouched() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val current = File(context.cacheDir, "teamcity_rest_v1.json")
        val legacy = File(context.cacheDir, "legacy.rxcache")
        current.delete()
        legacy.writeText("installed-cache")
        try {
            val store = AndroidResponseCacheStore(context)
            assertNull(store.read())
            store.write("{\"value\":\"你好\"}")
            assertEquals("{\"value\":\"你好\"}", AndroidResponseCacheStore(context).read())
            store.write("{}")
            assertEquals("{}", store.read())
            assertEquals("installed-cache", legacy.readText())
        } finally {
            current.delete()
            legacy.delete()
        }
    }
}
