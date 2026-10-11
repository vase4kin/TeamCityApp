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
import androidx.navigation3.runtime.NavKey
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import teamcityapp.libraries.navigation.DestinationRegistry
import teamcityapp.libraries.navigation.NavigationSnapshot

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DestinationIntentTestEntryPoint {
    fun registry(): DestinationRegistry
}

/** Assert typed arguments at the remaining native-to-root compatibility boundary. */
internal fun hasDestination(expected: NavKey) = destinationIntent { it == expected }
internal fun destinationIntent(matches: (NavKey) -> Boolean) = object : TypeSafeMatcher<Intent>() {
    override fun describeTo(description: Description) {
        description.appendText("root activity with the expected typed destination")
    }
    override fun matchesSafely(intent: Intent): Boolean {
        if (intent.component?.className != MainActivity::class.java.name) return false
        val payload = intent.getStringExtra(MainActivity.EXTRA_DESTINATION) ?: return false
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val registry = EntryPointAccessors.fromApplication(context, DestinationIntentTestEntryPoint::class.java).registry()
        return matches(registry.decodeDestination(payload))
    }
}

internal fun currentRootSnapshot(): NavigationSnapshot {
    var snapshot: NavigationSnapshot? = null
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        val root = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).filterIsInstance<MainActivity>().single()
        snapshot = root.navigationSnapshot
    }
    return requireNotNull(snapshot)
}
