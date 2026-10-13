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

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.splash.api.navigation.AccountActivationDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.features.splash.impl.navigation.AccountActivationRegistration
import teamcityapp.features.splash.impl.navigation.SplashNavigationDestinationRegistration
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.theme.TeamCityTheme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-notnight-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AccountActivationScreenTest {
    @get:Rule val compose = createComposeRule()
    private class Accounts : AccountFlowNavigation {
        val activations = mutableListOf<Pair<NavKey, Boolean>>()
        val homes = mutableListOf<Boolean>()
        override fun activate(destination: NavKey, resetSession: Boolean) {
            activations += destination to resetSession
        }
        override fun home(resetSession: Boolean) {
            homes += resetSession
        }
        override fun login() = error("Unexpected login")
    }
    private val accounts = Accounts()
    private var drawReadyReports = 0

    @Test fun switchingShowsOrdinaryProgressWithoutReleasingInitialPreparation() {
        show(AccountActivationDestination(resetSession = false))
        compose.onNodeWithTag("splash:loading").assertIsDisplayed()
        compose.onNodeWithContentDescription("TeamCityApp").assertDoesNotExist()
        compose.onNodeWithText("Try again").assertDoesNotExist()
        assertEquals(0, drawReadyReports)
    }

    @Test fun activationFailureReleasesSplashAndRetriesTheOriginalDestinationAndResetPolicy() {
        show(AccountActivationDestination(failed = true, resetSession = false, destination = SplashNavigationDestination))
        compose.onNodeWithTag("splash:error").assertIsDisplayed()
        assertTrue(drawReadyReports > 0)
        compose.onNodeWithText("Try again").performClick()
        assertEquals(listOf(SplashNavigationDestination to false), accounts.activations)
        assertTrue(accounts.homes.isEmpty())
    }

    @Test fun activationFailureWithoutExplicitDestinationRetriesHomeWithOriginalResetPolicy() {
        show(AccountActivationDestination(failed = true, resetSession = true))
        compose.onNodeWithText("Try again").performClick()
        assertEquals(listOf(true), accounts.homes)
        assertTrue(accounts.activations.isEmpty())
    }

    private fun show(destination: AccountActivationDestination) {
        compose.setContent {
            CompositionLocalProvider(LocalNavigationDrawReady provides { drawReadyReports++ }) {
                TeamCityTheme {
                    val registration = AccountActivationRegistration(accounts)
                    val registry = DestinationRegistry(setOf(registration, SplashNavigationDestinationRegistration(accounts)))
                    val navigator = rememberNavigator(destination, registry)
                    NavigationHost(navigator, registry, onExit = {})
                }
            }
        }
    }
}
