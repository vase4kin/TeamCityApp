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

package teamcityapp.features.change.router

import android.net.Uri
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.Storage

interface ChangeRouter {
    fun openUrl(url: String)
    fun openDiffView(id: String, fileName: String)
    fun bind()
    fun unbind()
}

private const val PATH_DIFF_VIEW = "diffView.html"
private const val PARAM_ID = "id"
private const val PARAM_FILE_NAME = "vcsFileName"

class ChangeRouterImpl(
    private val chromeCustomTabs: ChromeCustomTabs,
    private val storage: Storage
) : ChangeRouter {
    override fun openUrl(url: String) {
        chromeCustomTabs.launchUrl(url)
    }

    override fun openDiffView(id: String, fileName: String) {
        val serverUrl = storage.activeUser.teamcityUrl
        val diffUrl = Uri.parse(serverUrl)
            .buildUpon()
            .appendPath(PATH_DIFF_VIEW)
            .appendQueryParameter(PARAM_ID, id)
            .appendQueryParameter(PARAM_FILE_NAME, fileName)
            .build()
            .toString()
        chromeCustomTabs.launchUrl(diffUrl)
    }

    override fun bind() {
        chromeCustomTabs.initCustomsTabs()
    }

    override fun unbind() {
        chromeCustomTabs.unbindCustomsTabs()
    }
}
