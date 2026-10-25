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

package teamcityapp.shared

import androidx.compose.runtime.mutableStateOf
import kotlin.test.*
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.home.api.*
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.libraries.navigation.*

class SharedAppLaunchTest {
    @Test fun staleAccountAndSignedOutPrivateLaunchesCannotChangeOwners() {
        val state = mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("login", LoginDestination)), sessionKey = "signed-out"))
        val nav = Navigator(state)
        val before = nav.snapshot
        assertFalse(nav.acceptLaunch(SharedAppLaunch("old", BuildHistoryDestination("build", "Build"), "alice"), "signed-out", false))
        assertFalse(nav.acceptLaunch(SharedAppLaunch("private", BuildHistoryDestination("build", "Build"), "signed-out"), "signed-out", false))
        assertEquals(before, nav.snapshot)
        assertTrue(nav.acceptLaunch(SharedAppLaunch("add", CreateAccountDestination, "signed-out"), "signed-out", false))
        assertEquals(CreateAccountDestination, nav.current.destination)
    }

    @Test fun aChildPaneSelectsFavoritesWithoutLosingItsHistoryOwner() {
        val home = NavigationEntry("home", HomeDestination())
        val root = Navigator(mutableStateOf(NavigationSnapshot(listOf(home), sessionKey = "alice")))
        val history = NavigationEntry("history", BuildHistoryDestination("build", "Build"))
        val childState = mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("project", teamcityapp.features.navigation.api.navigation.ProjectDestination()), history), sessionKey = "alice/project"))
        val child = Navigator(childState, parent = root)
        child.selectHome(HomeTab.Favorites)
        assertEquals(listOf(home), root.snapshot.entries)
        assertEquals(history, child.current)
        assertEquals(HomeTabSelection(HomeTab.Favorites), root.consumeResult(home.id, HomeTabSelection::class))
        assertNull(root.consumeResult(home.id, HomeTabSelection::class))
    }

    @Test fun homeLaunchRetainsItsLiveOwnerAndReturnsTypedTabSelectionOnce() {
        val home = NavigationEntry("home", HomeDestination())
        val history = NavigationEntry("history", BuildHistoryDestination("build", "Build"))
        val state = mutableStateOf(NavigationSnapshot(listOf(home, history), sessionKey = "alice"))
        val nav = Navigator(state)
        assertTrue(nav.acceptLaunch(SharedAppLaunch("select", HomeDestination(HomeTab.Agents), "alice"), "alice", true))
        assertEquals(listOf(home), nav.snapshot.entries)
        assertEquals(HomeTabSelection(HomeTab.Agents), nav.consumeResult(home.id, HomeTabSelection::class))
        assertNull(nav.consumeResult(home.id, HomeTabSelection::class))
    }
}
