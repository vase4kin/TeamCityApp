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

package teamcityapp.features.splash.api

/** One account-presence snapshot; credentials and persistence details stay in the app adapter. */
interface SplashRepository {
    suspend fun hasAccounts(): Boolean
}
enum class SplashDestination { Home, Login }

/** The stable Android launcher alias, retained through the feature module migration. */
object SplashEntryPoint {
    const val LAUNCHER_ACTIVITY = "teamcityapp.features.splash.view.SplashActivity"
}
