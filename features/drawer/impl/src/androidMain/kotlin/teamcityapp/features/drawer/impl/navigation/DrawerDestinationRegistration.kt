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

package teamcityapp.features.drawer.impl.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.drawer.api.navigation.DrawerDestination
import teamcityapp.features.drawer.impl.DrawerRoute
import teamcityapp.features.drawer.impl.router.DrawerRouter
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.features.settings.api.navigation.SettingsDestination
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.resources.R as SharedR

class DrawerDestinationRegistration @Inject constructor(private val accounts: AccountFlowNavigation, private val rating: AppRating) : FeatureDestination<DrawerDestination>(DrawerDestination::class, DrawerDestination.serializer()) {

    @Composable override fun Content(destination: DrawerDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val gate = LocalSheetInteractions.current
        val router = remember(activity, navigator, entryId, gate) {
            val tabs = ChromeCustomTabsImpl(activity)
            object : DrawerRouter {
                override fun attach() = tabs.initCustomsTabs()
                override fun detach() = tabs.unbindCustomsTabs()
                override fun setInteractionsEnabled(enabled: Boolean) {
                    gate?.updateEnabled(enabled)
                }
                override fun openHome() {
                    accounts.home()
                }
                override fun openAbout() {
                    navigator.push(AboutDestination)
                }
                override fun openSettings() {
                    navigator.push(SettingsDestination)
                }
                override fun openAddAccount() {
                    navigator.push(CreateAccountDestination)
                }
                override fun openManageAccounts() {
                    navigator.push(ManageAccountsDestination)
                }
                override fun openPrivacy() = tabs.launchUrl(activity.getString(SharedR.string.about_app_url_privacy))
                override fun openRate() = rating.open(activity)
            }
        }
        DrawerRoute(router, onNavigateUp = { navigator.pop() })
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DrawerDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: DrawerDestinationRegistration): DestinationRegistration
}
