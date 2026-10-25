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

package com.github.vase4kin.teamcityapp.navigation

import android.app.Application
import java.io.File
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import teamcityapp.libraries.shared_data.ArtifactTarget

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AndroidArtifactFileStoreTest {
    private val context = RuntimeEnvironment.getApplication()
    private val store = AndroidArtifactFileStore(context)
    private val targets = mutableListOf<ArtifactTarget>()
    private fun target(name: String) = ArtifactTarget(name).also { targets += it }
    private fun folder(target: ArtifactTarget) = File(context.cacheDir, "TeamCityArtifacts/${target.transferId}")

    @After fun cleanup() {
        targets.forEach { folder(it).deleteRecursively() }
    }

    @Test fun sameNamedTransfersKeepBothCompletedFilesAndCannotReuseAnOwnedDirectory() {
        fun save(value: String): File {
            val target = target("report.txt")
            val pending = store.create(target)
            try {
                val bytes = value.encodeToByteArray()
                pending.write(bytes, bytes.size)
                val output = File(pending.finish().path)
                assertThrows(IllegalStateException::class.java) { store.create(target) }
                return output
            } finally {
                pending.close()
            }
        }
        val first = save("first")
        val second = save("second")
        assertNotEquals(first.path, second.path)
        assertEquals("first", first.readText())
        assertEquals("second", second.readText())
        assertEquals("report.txt", second.name)
    }

    @Test fun abandonedWritesRemoveTheirDirectoryAndCloseIsIdempotent() {
        val target = target("partial.txt")
        val pending = store.create(target)
        pending.write(byteArrayOf(1, 2), 2)
        pending.close()
        pending.close()
        assertFalse(folder(target).exists())
        assertThrows(IllegalStateException::class.java) { pending.finish() }
        assertThrows(IllegalStateException::class.java) { pending.write(byteArrayOf(1), 1) }
    }
}
