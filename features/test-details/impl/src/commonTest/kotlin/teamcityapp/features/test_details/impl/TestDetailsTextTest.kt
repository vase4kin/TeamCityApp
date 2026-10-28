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

package teamcityapp.features.test_details.impl

import kotlin.test.Test
import kotlin.test.assertEquals
import teamcityapp.features.test_details.impl.resources.*

class TestDetailsTextTest {
    @Test fun contentKeepsMarkupWhitespaceAndLineEndingsUntouched() {
        val details = "  <tag> &lt; literal >\r\n\tfirst   second\n\nlast \n"
        assertEquals(details, TestDetailsUiState.Content(details).details)
    }

    @Test fun searchIsCaseInsensitiveAndReturnsOriginalOffsets() {
        assertEquals(listOf(0..4, 13..17), findTestDetailsMatches("Error\n    at ERROR", "error"))
    }

    @Test fun searchTreatsMarkupAndRegexCharactersLiterally() {
        assertEquals(listOf(6..9), findTestDetailsMatches("value <.*> end", "<.*>"))
    }

    @Test fun emptyAndMissingQueriesHaveNoMatches() {
        assertEquals(emptyList<IntRange>(), findTestDetailsMatches("output", ""))
        assertEquals(emptyList<IntRange>(), findTestDetailsMatches("output", "absent"))
    }

    @Test fun repeatedMatchesDoNotOverlap() {
        assertEquals(listOf(0..1, 2..3), findTestDetailsMatches("aaaaa", "aa"))
    }

    @Test fun whitespaceQueriesSearchTheUnmodifiedOutput() {
        assertEquals(listOf(1..2), findTestDetailsMatches("a\n\tb", "\n\t"))
    }
}
