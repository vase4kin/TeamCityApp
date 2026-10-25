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

package com.github.vase4kin.teamcityapp.di.modules

import android.app.Application
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.http.Url
import teamcityapp.libraries.network.PlatformTeamCityClientFactory
import teamcityapp.libraries.network.TeamCityClientFactory

@Module
@InstallIn(SingletonComponent::class)
object SharedClientModule {
    @Provides fun clients(app: Application): TeamCityClientFactory = TeamCityClientFactory { identity ->
        // Mock builds remain offline for external origins. Only the task's explicit
        // loopback auth/security/HTTP fixtures use the real platform engine.
        if (Url(identity.serverUrl).host in setOf("localhost", "127.0.0.1", "::1", "[::1]", "10.0.2.2")) {
            PlatformTeamCityClientFactory.create(identity)
        } else {
            sharedMockClient {
                val application = app as TeamCityApplicationBase
                if (application.appInjector.sharedUserStorage().userAccounts.isEmpty()) FakeTeamCityServiceImpl() else application.restApiInjector.teamCityService()
            }
        }
    }
}
