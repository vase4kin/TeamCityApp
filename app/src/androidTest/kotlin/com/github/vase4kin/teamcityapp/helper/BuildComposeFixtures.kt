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

import android.content.Intent
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.google.gson.Gson
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import teamcityapp.libraries.builds.*

/** Full snapshots ensure navigation tests detect a summary-only or stale-build bridge. */
internal object BuildComposeFixtures {
    val mapper = AppBuildLaunchMapper()
    val incoming = BuildLaunchData(
        "42", "/queue/42", webUrl = "https://ci.example/old", number = "old-number", state = "queued",
        buildTypeId = "old-config", configuration = BuildConfigurationData("old-config", "Old configuration", "old-project", "Old project"),
        branchName = "old-branch", queuedDate = "20160621T233008+0700", waitReason = "Waiting for agent",
        changes = BuildCollectionLink("/changes/42", 2), artifacts = BuildCollectionLink("/artifacts/42"),
        tests = BuildTests("/tests/42", passed = 1), properties = BuildProperties(emptyList()),
        snapshotDependencies = BuildCollectionLink("/snapshot/42", 3)
    )
    val loaded = incoming.copy(
        href = "/builds/42", webUrl = "https://ci.example/current", number = "latest-number", state = "running", status = "SUCCESS",
        statusText = "Running current build", branchName = "current-branch", buildTypeId = "current-config",
        configuration = BuildConfigurationData("current-config", "Current configuration", "current-project", "Current project", "/config/current"),
        startDate = "20160621T230008+0700", triggered = BuildTrigger("user", user = BuildUser("alice", "Alice", "user-id", "/users/alice")),
        properties = BuildProperties(listOf(BuildProperty("env", "production", own = true, id = "property-id", href = "/property/env")), "properties-id", "/properties/42"),
        agent = BuildAgent("agent-id", "Agent one", "/agents/one"), cleanSources = true, queueAtTop = true, pinned = true
    )
    val finished = loaded.copy(state = "finished", statusText = "Current build succeeded", finishDate = "20160621T233008+0700")
    fun legacy(build: BuildLaunchData): Build = mapper.toLegacyBuild(build)
    fun intent(build: BuildLaunchData = incoming) = Intent(InstrumentationRegistry.getInstrumentation().targetContext, BuildDetailsActivity::class.java).putExtras(
        Bundle().apply {
            putSerializable(BundleExtractorValues.BUILD, legacy(build))
            putString(BundleExtractorValues.NAME, "Incoming configuration name")
        }
    )
    fun fullPayload(key: String, build: BuildLaunchData) = object : TypeSafeMatcher<Intent>() {
        override fun describeTo(description: Description) {
            description.appendText("complete serialized build in $key")
        }

        @Suppress("DEPRECATION")
        override fun matchesSafely(intent: Intent): Boolean {
            val payload = intent.getSerializableExtra(key) as? Build ?: return false
            return Gson().toJsonTree(legacy(build)) == Gson().toJsonTree(payload)
        }
    }
}
