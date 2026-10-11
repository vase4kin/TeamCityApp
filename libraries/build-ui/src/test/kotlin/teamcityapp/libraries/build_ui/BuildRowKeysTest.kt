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

package teamcityapp.libraries.build_ui

import org.junit.Assert.*
import org.junit.Test
import teamcityapp.libraries.builds.BuildLaunchData

class BuildRowKeysTest {
    @Test fun insertionsAndSortingDoNotChangeExistingUniqueRowIdentity() {
        val first = BuildLaunchData("1", "/builds/1")
        val second = BuildLaunchData("2", "/builds/2")
        val third = BuildLaunchData("3", "/builds/3")
        val original = buildRowKeys(listOf(first, second))
        val reordered = buildRowKeys(listOf(third, second, first))
        assertEquals(original[0], reordered[2])
        assertEquals(original[1], reordered[1])
    }

    @Test fun repeatedRowsAndSameIdDifferentUrlsHaveDistinctKeys() {
        val first = BuildLaunchData("1", "/builds/1")
        val keys = buildRowKeys(listOf(first, first, first.copy(href = "/builds/other")))
        assertEquals(keys.size, keys.distinct().size)
        assertEquals(buildRowKeys(listOf(first)).single(), keys.first())
    }

    @Test fun embeddedSeparatorsCannotMakeDifferentIdentitiesCollide() {
        val keys = buildRowKeys(listOf(BuildLaunchData("a:b", "c"), BuildLaunchData("a", "b:c")))
        assertEquals(2, keys.distinct().size)
    }
}
