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
import teamcityapp.features.create_account.api.CreateAccountTracker
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.create_account.impl.*
import teamcityapp.features.create_account.impl.router.CreateAccountRouter
import teamcityapp.features.drawer.impl.*
import teamcityapp.features.drawer.impl.router.DrawerRouter
import teamcityapp.features.drawer.impl.tracker.DrawerTracker
import teamcityapp.features.manage_accounts.api.AccountDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.manage_accounts.impl.*
import teamcityapp.features.manage_accounts.impl.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.impl.tracker.ManageAccountsTracker
import teamcityapp.libraries.accounts.AccountSessionState
import teamcityapp.libraries.navigation.*

internal fun nativeAccountEntries(model: SharedAppViewModel): Set<DestinationRegistration> = setOf(
    SharedEntry(ManageAccountsDestination::class, ManageAccountsDestination.serializer()) { _, nav, _ ->
        val diagnostics = LocalNativePlatform.current.diagnostics
        val tracker = remember(diagnostics) {
            object : ManageAccountsTracker {
                override fun trackView() = diagnostics.event("screen_manage_accounts")
                override fun trackAccountRemove() = diagnostics.event("account_removed")
                override fun trackUserClicksOnSslDisabledWarning() = diagnostics.event("account_ssl_warning")
            }
        }
        val vm = viewModel { ManageAccountsViewModel(NativeManageAccountsRepository(model.sessions), tracker) }
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
        val diagnostics = LocalNativePlatform.current.diagnostics
        val tracker = remember(diagnostics) {
            object : CreateAccountTracker {
                override fun trackView() = diagnostics.event("screen_add_account")
                override fun trackUserLoginSuccess(isSslEnabled: Boolean) = diagnostics.event("account_added")
                override fun trackGuestUserLoginSuccess(isSslEnabled: Boolean) = diagnostics.event("guest_account_added")
                override fun trackUserLoginFailed(errorMessage: String) = diagnostics.event("account_add_failed")
                override fun trackGuestUserLoginFailed(errorMessage: String) = diagnostics.event("guest_account_add_failed")
                override fun trackUserDataSaveFailed() = diagnostics.event("account_save_failed")
            }
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
internal fun NativeAccountContent(model: SharedAppViewModel, nav: Navigator) {
    val platform = LocalNativePlatform.current
    val diagnostics = platform.diagnostics
    val tracker = remember(diagnostics) {
        object : DrawerTracker {
            override fun trackChangeAccount() = diagnostics.event("account_selected")
            override fun trackOpenPrivacy() = diagnostics.event("account_privacy")
            override fun trackRateTheApp() = diagnostics.event("account_rating")
            override fun trackOpenAbout() = diagnostics.event("account_about")
            override fun trackOpenAddNewAccount() = diagnostics.event("account_add")
            override fun trackOpenManageAccounts() = diagnostics.event("account_manage")
            override fun trackOpenSettings() = diagnostics.event("account_settings")
            override fun trackView() = diagnostics.event("screen_account")
        }
    }
    val vm = viewModel { DrawerViewModel(NativeAccountSwitcherRepository(model.sessions), tracker) }
    val router = remember(platform, nav) {
        object : DrawerRouter {
            override fun attach() = Unit
            override fun detach() = Unit
            override fun setInteractionsEnabled(enabled: Boolean) = Unit
            override fun openHome() = Unit
            override fun openAbout() {
                nav.push(NativeAboutDestination)
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
                platform.browser("https://telegra.ph/TeamCityApp-Private-Policy-09-18")
            }
            override fun openRate() {
                platform.rate()
            }
        }
    }
    SharedDrawerRoute(router, vm, onNavigateUp = { nav.pop() }, showSignOut = model.sessions.state.value is AccountSessionState.Active)
}
