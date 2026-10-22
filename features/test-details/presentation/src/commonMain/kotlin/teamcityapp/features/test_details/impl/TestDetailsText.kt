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

import teamcityapp.features.test_details.presentation.resources.*

/** Literal, case-insensitive, non-overlapping matches in the untouched server output. */
fun findTestDetailsMatches(details: String, query: String): List<IntRange> {
    if (query.isEmpty()) return emptyList()
    return buildList {
        var start = details.indexOf(query, ignoreCase = true)
        while (start >= 0) {
            add(start until start + query.length)
            start = details.indexOf(query, startIndex = start + query.length, ignoreCase = true)
        }
    }
}
