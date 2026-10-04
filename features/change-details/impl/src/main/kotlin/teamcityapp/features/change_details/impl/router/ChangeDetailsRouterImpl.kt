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

package teamcityapp.features.change_details.impl.router

import android.app.Activity
import androidx.core.net.toUri
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.Storage

/** UI-scoped browser adapter; never retained by the ViewModel. */
class ChangeDetailsRouterImpl(
    private val activity: Activity,
    private val chromeTabs: ChromeCustomTabs,
    private val storage: Storage,
) : ChangeDetailsRouter {
    override fun start() = chromeTabs.initCustomsTabs()
    override fun stop() = chromeTabs.unbindCustomsTabs()
    override fun close() = activity.finish()
    override fun openUrl(url: String) {
        chromeTabs.launchUrl(url)
    }
    override fun openDiff(id: String, fileName: String) {
        val url = storage.activeUser.teamcityUrl.toUri().buildUpon()
            .appendPath("diffView.html")
            .appendQueryParameter("id", id)
            .appendQueryParameter("vcsFileName", fileName)
            .build().toString()
        chromeTabs.launchUrl(url)
    }
}
