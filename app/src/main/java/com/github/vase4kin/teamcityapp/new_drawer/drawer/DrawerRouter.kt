/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.new_drawer.drawer

import android.content.Intent
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.about.AboutActivity
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity
import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabs
import com.github.vase4kin.teamcityapp.new_drawer.view.DrawerBottomSheetDialogFragment

interface DrawerRouter {

    fun openPrivacy()

    fun openWebsite()

    fun openAbout()

    fun openAddNewAccount()

    fun openManageAccounts()
}

class DrawerRouterImpl(
    private val fragment: DrawerBottomSheetDialogFragment,
    private val chromeCustomTabs: ChromeCustomTabs
) :
    DrawerRouter {

    override fun openPrivacy() {
        val privacyUrl = fragment.getString(R.string.about_app_url_privacy)
        chromeCustomTabs.launchUrl(privacyUrl)
    }

    override fun openWebsite() {
        val website = fragment.getString(R.string.about_app_url_web)
        chromeCustomTabs.launchUrl(website)
    }

    override fun openAbout() {
        AboutActivity.start(fragment.requireActivity())
        fragment.dismiss()
    }

    override fun openAddNewAccount() {
        val activity = fragment.requireActivity()
        activity.startActivity(Intent(activity, CreateAccountActivity::class.java))
        fragment.dismiss()
    }

    override fun openManageAccounts() {
        AccountListActivity.start(fragment.requireActivity())
        fragment.dismiss()
    }
}