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

private val htmlWhitespace = Regex("[ \t\r\n\u000C]+")

/** Match whitespace in the previous escaped HTML body without introducing an HTML renderer. */
internal fun formatTestDetails(details: String): String {
    val escapedSpaces = buildString {
        details.forEachIndexed { index, character ->
            // Html.escapeHtml preserves repeated spaces with nonbreaking spaces.
            append(if (character == ' ' && details.getOrNull(index + 1) == ' ') '\u00A0' else character)
        }
    }
    // HTML's normal whitespace collapses line breaks and tabs; literal markup stays literal.
    return escapedSpaces.replace(htmlWhitespace, " ").trim(' ')
}
