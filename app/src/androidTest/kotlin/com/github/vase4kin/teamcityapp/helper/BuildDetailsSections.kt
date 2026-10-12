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

/** Opens a grouped section as its own root navigation visit. */
fun ComposeTestRule.tapBuildDetailsSection(title: String) {
    repeat(8) {
        if (currentRootSnapshot().entries.last().destination !is teamcityapp.features.build_details.api.BuildDetailsDestination) androidx.test.espresso.Espresso.pressBack()
    }
    waitUntil(10_000) { onAllNodesWithTag("overview:list").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    val label = title.substringBefore(" (")
    if (label == "Overview") return
    val section = when (label) {
        "Snapshot dependencies", "Dependencies" -> "Snapshots"
        else -> label
    }
    val tag = "overview:section:$section"
    // Align the row near the top, clear of the controls floating over the bottom.
    onNodeWithTag("overview:list").performScrollToKey(teamcityapp.features.build_details.api.BuildDetailsSection.valueOf(section))
    onNodeWithTag(tag).performClick()
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

/** Waits for the loaded action before tapping its stable semantics identifier. */
fun ComposeTestRule.tapBuildAction(action: String) {
    waitUntil(10_000) { onAllNodesWithTag("details:action:$action").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    onNodeWithTag("details:action:$action").performClick()
    waitForIdle()
}
