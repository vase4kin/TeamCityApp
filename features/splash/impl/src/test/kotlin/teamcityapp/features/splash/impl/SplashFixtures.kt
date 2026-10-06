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

package teamcityapp.features.splash.impl
import teamcityapp.features.splash.api.SplashDestination
internal fun splashFixture(name: String): SplashUiState = when (name) {
    "loading" -> SplashUiState.Loading
    "error" -> SplashUiState.Error
    "home" -> SplashUiState.Ready(SplashDestination.Home)
    "login" -> SplashUiState.Ready(SplashDestination.Login)
    "navigated" -> SplashUiState.Navigated
    else -> error("Unknown Splash fixture: $name")
}
