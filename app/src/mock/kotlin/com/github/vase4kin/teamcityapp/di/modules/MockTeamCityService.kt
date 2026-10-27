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

import com.github.vase4kin.teamcityapp.api.TeamCityService
import javax.inject.Inject
import javax.inject.Singleton

/** Offline fixture seam; never creates or replaces an account networking graph. */
@Singleton
class MockTeamCityService @Inject constructor() {
    @Volatile var service: TeamCityService = FakeTeamCityServiceImpl()
}

class MockHttpFailure(val status: Int) : Exception("Fixture HTTP $status")
