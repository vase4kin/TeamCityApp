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

package com.github.vase4kin.teamcityapp.helper

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

/** Selects the root's actual Compose tab, including localized count suffixes. */
fun ComposeTestRule.tapBuildDetailsTab(title: String) {
    val tab = SemanticsMatcher("Build Details tab") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("details:tab:") == true
    } and hasText(title, substring = true)
    waitUntil(10_000) { onAllNodes(tab).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    onNode(tab).performScrollTo().performClick()
    waitForIdle()
}

/** Waits for real visibility, including asynchronous dialog/snackbar entrance. */
fun ComposeTestRule.awaitDisplayedText(value: String) {
    waitUntil(10_000) {
        val matches = onAllNodesWithText(value)
        matches.fetchSemanticsNodes(atLeastOneRootRequired = false).indices.any { index ->
            runCatching { matches[index].assertIsDisplayed() }.isSuccess
        }
    }
}
