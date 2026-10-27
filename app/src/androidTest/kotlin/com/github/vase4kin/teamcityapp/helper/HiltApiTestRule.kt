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

import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.rules.ExternalResource

/** Initializes Hilt and supplies responses to the common Ktor mock engine. */
class HiltApiTestRule(
    private val hilt: HiltAndroidRule,
    private val service: () -> TeamCityService = { FakeTeamCityServiceImpl() }
) : ExternalResource() {
    override fun before() {
        hilt.inject()
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        // Each fixture case seeds installed account/preferences state independently. Clear
        // only the mock app's public sidecars so a previous case cannot override those seeds.
        kotlinx.coroutines.runBlocking {
            val state = dagger.hilt.EntryPoints.get(app, SharedRootTestState::class.java)
            state.navigation().write("")
            (state.accounts() as? teamcityapp.libraries.session.AndroidAccountPreferencesStorage)?.resetPreferencesForTesting()
        }
        app.appInjector.themeController().start()
        dagger.hilt.EntryPoints.get(app, MockWireTestState::class.java).fixtures().service = service()
        kotlinx.coroutines.runBlocking {
            dagger.hilt.EntryPoints.get(app, SharedRootTestState::class.java).cache().write("{}")
        }
    }
    override fun after() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.themeController().stop()
        kotlinx.coroutines.runBlocking { app.appInjector.themePreferencesStore().close() }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface SharedRootTestState {
    fun cache(): teamcityapp.libraries.network.ResponseCacheStore
    fun accounts(): teamcityapp.libraries.accounts.AccountStorage
    fun navigation(): com.github.vase4kin.teamcityapp.navigation.AndroidSavedNavigationStore
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface MockWireTestState {
    fun fixtures(): com.github.vase4kin.teamcityapp.di.modules.MockTeamCityService
}
