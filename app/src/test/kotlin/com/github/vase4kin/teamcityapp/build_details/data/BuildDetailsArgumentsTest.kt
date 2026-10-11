/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.build_details.data

import android.app.Application
import android.os.Bundle
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.overview.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.builds.BuildProperties

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildDetailsArgumentsTest {
    private val mapper = AppBuildLaunchMapper()

    @Test fun incomingPayloadAndNameInitializeOneCurrentSnapshot() {
        val arguments = BuildDetailsArguments(overviewArguments())
        assertEquals("Incoming name", arguments.initialName)
        assertEquals(incomingOverview, mapper.toLaunchData(requireNotNull(arguments.initialBuild)))
        assertSame(arguments.initialDetails, arguments.current)
    }

    @Test fun loadedBuildReplacesCurrentActionsButKeepsIncomingTabSnapshot() {
        val arguments = BuildDetailsArguments(overviewArguments())
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        assertEquals(loadedOverview, mapper.toLaunchData(requireNotNull(arguments.currentBuild)))
        assertEquals("/builds/42", arguments.current.href)
        assertEquals("/queue/42", arguments.initialDetails.href)
        assertEquals("/old-tests", arguments.initialDetails.testsHref)
        assertEquals("/latest-tests", arguments.current.testsHref)
        assertEquals("Incoming name", arguments.initialName)
    }

    @Test fun currentBuildSurvivesRecreationWhileOverviewIsHidden() {
        val arguments = BuildDetailsArguments(overviewArguments())
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        val saved = Bundle().also(arguments::saveCurrent)
        val recreated = BuildDetailsArguments(overviewArguments())
        recreated.restoreCurrent(saved)
        assertEquals(loadedOverview, mapper.toLaunchData(requireNotNull(recreated.currentBuild)))
        assertEquals(incomingOverview, mapper.toLaunchData(requireNotNull(recreated.initialBuild)))
    }

    @Test fun savedCurrentFromAnotherBuildDoesNotOverrideNewArguments() {
        val arguments = BuildDetailsArguments(overviewArguments())
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        val saved = Bundle().also(arguments::saveCurrent)
        val other = incomingOverview.copy(id = "other", href = "/queue/other")
        val recreated = BuildDetailsArguments(overviewArguments(other))
        recreated.restoreCurrent(saved)
        assertEquals(other, mapper.toLaunchData(requireNotNull(recreated.currentBuild)))
    }

    @Test fun newIntentResetsTheSameScopedInstanceBeforeNativePresenterCreation() {
        val arguments = BuildDetailsArguments(overviewArguments())
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        val next = incomingOverview.copy(id = "next", href = "/builds/next", number = "next-number")
        arguments.resetIncoming(overviewArguments(next, "Next name"))
        assertEquals(next, mapper.toLaunchData(requireNotNull(arguments.currentBuild)))
        assertSame(arguments.initialDetails, arguments.current)
        assertEquals("Next name", arguments.initialName)
    }

    @Test fun missingPayloadUsesTheLegacyStubAndEmptyName() {
        val arguments = BuildDetailsArguments(null)
        assertNull(arguments.initialBuild)
        assertNull(arguments.currentBuild)
        assertEquals("", arguments.initialName)
        assertSame(BuildDetails.STUB, arguments.current)
        arguments.restoreCurrent(Bundle())
        assertSame(BuildDetails.STUB, arguments.current)
    }

    @Test fun restartPropertiesRetainWrapperPresenceAndMetadataInSavedCurrent() {
        val current = loadedOverview.copy(properties = BuildProperties(null, "properties-id", "/properties"))
        val arguments = BuildDetailsArguments(overviewArguments())
        arguments.publishLoaded(mapper.toLegacyBuild(current))
        val saved = Bundle().also(arguments::saveCurrent)
        val recreated = BuildDetailsArguments(overviewArguments())
        recreated.restoreCurrent(saved)
        assertEquals(current.properties, mapper.toLaunchData(requireNotNull(recreated.currentBuild)).properties)
        assertNotNull(recreated.current.properties)
        assertNull(recreated.current.properties?.properties)
    }
}
