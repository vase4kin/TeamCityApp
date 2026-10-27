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

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.create_account.impl.*
import teamcityapp.features.create_account.impl.router.CreateAccountRouter
import teamcityapp.features.create_account.impl.tracker.CreateAccountTrackerImpl
import teamcityapp.features.drawer.impl.*
import teamcityapp.features.drawer.impl.router.DrawerRouter
import teamcityapp.features.drawer.impl.tracker.DrawerTrackerImpl
import teamcityapp.features.manage_accounts.api.AccountDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.manage_accounts.impl.*
import teamcityapp.features.manage_accounts.impl.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.impl.tracker.ManageAccountsTrackerImpl
import teamcityapp.libraries.accounts.AccountSessionState
import teamcityapp.libraries.navigation.*

internal fun sharedAccountEntries(model: SharedAppViewModel): Set<DestinationRegistration> = setOf(
    SharedEntry(ManageAccountsDestination::class, ManageAccountsDestination.serializer()) { _, nav, _ ->
        val analytics = LocalNativePlatform.current.analytics
        val tracker = remember(analytics) {
            ManageAccountsTrackerImpl(analytics)
        }
        val vm = viewModel { ManageAccountsViewModel(AccountSessionManageAccountsRepository(model.sessions), tracker) }
        val router = remember(nav) {
            object : ManageAccountsRouter {
                override fun close() {
                    nav.pop()
                }
                override fun createAccount() {
                    nav.push(CreateAccountDestination)
                }

                // Account-generation replacement owns the root transition after removal.
                override fun navigate(destination: AccountDestination) {
                    if (destination == AccountDestination.Login && nav.snapshot.sessionKey == "signed-out" && model.sessions.state.value == AccountSessionState.SignedOut) {
                        nav.reset(teamcityapp.features.login.api.navigation.LoginDestination, "signed-out")
                    }
                }
            }
        }
        SharedManageAccountsRoute(router, vm)
    },
    SharedEntry(CreateAccountDestination::class, CreateAccountDestination.serializer()) { _, nav, _ ->
        val analytics = LocalNativePlatform.current.analytics
        val tracker = remember(analytics) {
            CreateAccountTrackerImpl(analytics)
        }
        val vm = viewModel { CreateAccountViewModel(model.authentication, tracker, checkSecureConnection = true) }
        val router = remember(nav) {
            object : CreateAccountRouter {
                override fun close() {
                    nav.pop()
                }

                // Persistence publishes the new account generation and replaces the root once.
                override fun openProjects() = Unit
            }
        }
        SharedCreateAccountRoute(router, vm)
    }
)

@Composable
internal fun SharedAccountContent(model: SharedAppViewModel, nav: Navigator) {
    val platform = LocalNativePlatform.current
    val analytics = platform.analytics
    val tracker = remember(analytics) {
        DrawerTrackerImpl(analytics)
    }
    val vm = viewModel { DrawerViewModel(AccountSessionDrawerRepository(model.sessions), tracker) }
    val router = remember(platform, nav) {
        object : DrawerRouter {
            override fun attach() = Unit
            override fun detach() = Unit
            override fun setInteractionsEnabled(enabled: Boolean) = Unit
            override fun openHome() = Unit
            override fun openAbout() {
                nav.push(AboutDestination)
            }
            override fun openSettings() {
                nav.push(teamcityapp.features.settings.api.navigation.SettingsDestination)
            }
            override fun openAddAccount() {
                nav.push(CreateAccountDestination)
            }
            override fun openManageAccounts() {
                nav.push(ManageAccountsDestination)
            }
            override fun openPrivacy() {
                platform.browser(ACCOUNT_PRIVACY_URL)
            }
            override fun openRate() {
                platform.rate()
            }
        }
    }
    SharedDrawerRoute(router, vm, onNavigateUp = { nav.pop() })
}
