/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.drawer.impl.router
import teamcityapp.features.drawer.api.router.DrawerAppRouter
import teamcityapp.features.drawer.impl.DrawerBottomSheetDialogFragment
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.resources.R as SharedR
class DrawerRouterImpl(
    private val fragment: DrawerBottomSheetDialogFragment,
    private val tabs: ChromeCustomTabs,
    private val appRouter: DrawerAppRouter,
    private val rating: AppRating
) : DrawerRouter {
    override fun attach() = tabs.initCustomsTabs()
    override fun detach() = tabs.unbindCustomsTabs()
    override fun setInteractionsEnabled(enabled: Boolean) = fragment.setInteractionsEnabled(enabled)
    override fun openHome() {
        appRouter.openHome(fragment.requireActivity())
        fragment.dismiss()
    }
    override fun openAbout() {
        appRouter.openAbout(fragment.requireActivity())
        fragment.dismiss()
    }
    override fun openSettings() {
        appRouter.openSettings(fragment.requireActivity())
        fragment.dismiss()
    }
    override fun openAddAccount() {
        appRouter.openAddAccount(fragment.requireActivity())
        fragment.dismiss()
    }
    override fun openManageAccounts() {
        appRouter.openManageAccounts(fragment.requireActivity())
        fragment.dismiss()
    }
    override fun openPrivacy() {
        tabs.launchUrl(fragment.getString(SharedR.string.about_app_url_privacy))
    }
    override fun openRate() {
        rating.open(fragment.requireActivity())
    }
}
