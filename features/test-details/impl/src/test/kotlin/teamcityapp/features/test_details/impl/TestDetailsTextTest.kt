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

import org.junit.Assert.assertEquals
import org.junit.Test

class TestDetailsTextTest {
    @Test fun markupAndEntitiesStayLiteral() {
        val details = "<script>alert('test')</script> &lt; literal >"
        assertEquals(details, formatTestDetails(details))
    }

    @Test fun lineBreaksTabsAndSingleLeadingSpacesMatchHtmlWhitespace() {
        assertEquals("first second third", formatTestDetails(" first\n\r\nsecond\tthird "))
    }

    @Test fun repeatedSpacesRemainVisibleAndNonbreaking() {
        assertEquals("first\u00A0\u00A0 second", formatTestDetails("first   second"))
        assertEquals("\u00A0 first", formatTestDetails("  first"))
    }
}
