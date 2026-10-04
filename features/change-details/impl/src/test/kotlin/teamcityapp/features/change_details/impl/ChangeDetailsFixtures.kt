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

package teamcityapp.features.change_details.impl

import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile

internal val fixture = ChangeDetails("123", "Preserve the Compose screen appearance", "Developer", "01 Oct 2026",
    listOf(ChangedFile("app/src/main/kotlin/example/Screen.kt", "changed"),
        ChangedFile("features/example/src/test/kotlin/example/ScreenTest.kt", "added"), ChangedFile("README.md", "removed")),
    "ef286eeca3ccdfbe8f4883ec0efa487889a616ec", "https://teamcity.example/change/123")
