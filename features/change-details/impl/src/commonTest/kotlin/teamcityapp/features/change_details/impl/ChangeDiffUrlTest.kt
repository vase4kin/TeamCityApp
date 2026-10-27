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

package teamcityapp.features.change_details.impl

import kotlin.test.*

class ChangeDiffUrlTest {
    @Test fun diffLinksKeepFileNamesAndIdsAsSingleQueryParameters() {
        val link = io.ktor.http.Url("https://teamcity.example/" + changeDiffUrl("42&next=other", "src/a b+c.kt?#fragment"))
        assertEquals(listOf("42&next=other"), link.parameters.getAll("id"))
        assertEquals(listOf("src/a b+c.kt?#fragment"), link.parameters.getAll("vcsFileName"))
        assertTrue(link.fragment.isEmpty())
    }
}
