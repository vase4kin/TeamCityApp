/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.drawer.drawer

import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import teamcityapp.features.drawer.R
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

class DrawerRouterImpl(
    private val fragment: DrawerBottomSheetDialogFragment,
    private val storage: Storage,
    private val chromeCustomTabs: ChromeCustomTabs,
    private val router: DrawerAppRouter
) :
    DrawerRouter {

    override fun openPrivacy() {
        val privacyUrl = fragment.getString(R.string.about_app_url_privacy)
        chromeCustomTabs.launchUrl(privacyUrl)
    }

    override fun openRateTheApp() {
        ConvenienceBuilder.createRateOnClickAction(fragment.requireActivity())
            .onClick()
    }

    override fun openAbout() {
        router.openAboutScreen()
        fragment.dismiss()
    }

    override fun openAddNewAccount() {
        router.openNewAccount()
        fragment.dismiss()
    }

    override fun openManageAccounts() {
        router.openManageAccounts()
        fragment.dismiss()
    }

    override fun switchToAccount(account: UserAccount) {
        storage.setUserActive(account.teamcityUrl, account.userName)
        router.openHomeActivity()
    }

    override fun openSettings() {
        router.openSettingsActivity()
        fragment.dismiss()
    }

    override fun openAgents() {
        router.openAgentsActivity()
        fragment.dismiss()
    }
}
