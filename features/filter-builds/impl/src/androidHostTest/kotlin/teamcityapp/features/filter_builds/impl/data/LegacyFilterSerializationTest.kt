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

package teamcityapp.features.filter_builds.impl.data

import com.github.vase4kin.teamcityapp.buildlist.filter.*
import java.io.*
import java.util.Base64
import org.junit.Assert.*
import org.junit.Test

class LegacyFilterSerializationTest {
    @Test fun legacyFilterSerializablePayloadKeepsItsClassAndEffectiveLocator() {
        val original = BuildListFilterImpl().apply {
            setBranch("feature/space & symbols")
            setPersonal(true)
            setPinned(true)
        }
        val bytes = ByteArrayOutputStream().also { buffer -> ObjectOutputStream(buffer).use { it.writeObject(original) } }.toByteArray()
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() } as BuildListFilter
        assertEquals("com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl", restored.javaClass.name)
        assertEquals(original.toLocator(), restored.toLocator())
        assertTrue(restored.toLocator().contains("branch:name:feature/space & symbols"))
    }

    @Test fun payloadWrittenBeforeTheOwnershipMoveStillDecodes() {
        assertEquals(-5671638589314539134L, ObjectStreamClass.lookup(BuildListFilterImpl::class.java).serialVersionUID)
        val bytes = Base64.getDecoder().decode("rO0ABXNyAERjb20uZ2l0aHViLnZhc2U0a2luLnRlYW1jaXR5YXBwLmJ1aWxkbGlzdC5maWx0ZXIuQnVpbGRMaXN0RmlsdGVySW1wbLFKSsCtgmGCAgAESQAKZmlsdGVyVHlwZVoACmlzUGVyc29uYWxaAAhpc1Bpbm5lZEwABmJyYW5jaHQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwAAAABwEBdAAXZmVhdHVyZS9zcGFjZSAmIHN5bWJvbHM=")
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() } as BuildListFilter
        assertEquals("state:any,canceled:any,failedToStart:any,branch:name:feature/space & symbols,personal:true,pinned:true,count:10", restored.toLocator())
    }
}
