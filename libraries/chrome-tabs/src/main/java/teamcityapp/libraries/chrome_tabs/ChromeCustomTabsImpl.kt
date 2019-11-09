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

package teamcityapp.libraries.chrome_tabs

import android.app.Activity
import android.content.ComponentName
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.content.ContextCompat

private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"

/**
 * Impl of [ChromeCustomTabs]
 */
class ChromeCustomTabsImpl(private val activity: Activity) :
    ChromeCustomTabs {

    private var client: CustomTabsClient? = null
    private var customTabsServiceConnection: CustomTabsServiceConnection? = null
    private var customTabsSession: CustomTabsSession? = null
    private var customTabsIntent: CustomTabsIntent? = null

    /**
     * {@inheritDoc}
     */
    override fun initCustomsTabs() {
        /*
            Setup Chrome Custom Tabs
         */
        customTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {

                // Pre-warming
                client = customTabsClient
                customTabsClient.warmup(0L)
                // Initialize a session as soon as possible.
                customTabsSession = customTabsClient.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                client = null
            }
        }

        CustomTabsClient.bindCustomTabsService(
            activity,
            CUSTOM_TAB_PACKAGE_NAME,
            customTabsServiceConnection
        )

        customTabsIntent = CustomTabsIntent.Builder(customTabsSession)
            .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            .setShowTitle(true)
            .build()
        /*
            End custom tabs setup
         */
    }

    /**
     * {@inheritDoc}
     */
    override fun unbindCustomsTabs() {
        val customTabsServiceConnection = this.customTabsServiceConnection ?: return
        activity.unbindService(customTabsServiceConnection)
        client = null
        customTabsSession = null
    }

    /**
     * {@inheritDoc}
     */
    override fun launchUrl(url: String) {
        customTabsIntent?.launchUrl(activity, Uri.parse(url))
    }
}
