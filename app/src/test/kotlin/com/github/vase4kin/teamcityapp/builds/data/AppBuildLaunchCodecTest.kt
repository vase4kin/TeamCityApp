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

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.BuildProperties

class AppBuildLaunchCodecTest {
    private val codec = AppBuildLaunchCodec(AppBuildLaunchMapper())

    @Test fun encodeRetainsInstalledSerializedBuildClass() {
        val encoded = codec.encode(completeLaunch())
        assertEquals(Build::class.java, encoded.javaClass)
        assertEquals("com.github.vase4kin.teamcityapp.buildlist.api.Build", encoded.javaClass.name)
        assertEquals(completeLaunch(), codec.decode(encoded))
    }

    @Test fun decodeAcceptsExistingLegacySerializedCompletePayload() {
        val restored = serialized(legacyBuild(completeBuildJson))
        assertEquals(Build::class.java, restored.javaClass)
        assertEquals(completeLaunch(), codec.decode(restored))
    }

    @Test fun encodedPayloadSurvivesJavaSerializationWithAllDetailsReferencesAndRestartProperties() {
        val restored = serialized(codec.encode(completeLaunch())) as Build
        assertNotNull(restored.changes)
        assertNotNull(restored.artifacts)
        assertNotNull(restored.testOccurrences)
        assertNotNull(restored.snapshotBuilds)
        assertNotNull(restored.properties)
        assertEquals(completeLaunch(), codec.decode(restored))
    }

    @Test fun partialLegacyPayloadWithAbsentSectionsRemainsPartial() {
        assertEquals(BuildLaunchData("1", "/builds/1"), codec.decode(serialized(legacyBuild("""{"id":"1","href":"/builds/1"}"""))))
    }

    @Test fun serializationPreservesNullListWrapperRatherThanCreatingAnEmptyList() {
        val build = BuildLaunchData("1", "/builds/1", properties = BuildProperties(null, "p", "/properties"))
        assertEquals(build, codec.decode(serialized(codec.encode(build))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unrelatedSerializablePayloadIsRejected() {
        codec.decode("1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun idOnlyLegacyPayloadIsRejected() {
        codec.decode(legacyBuild("""{"id":"1"}"""))
    }

    private fun serialized(payload: Serializable): Serializable {
        val bytes = ByteArrayOutputStream().use { output ->
            ObjectOutputStream(output).use { it.writeObject(payload) }
            output.toByteArray()
        }
        return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Serializable }
    }
}
