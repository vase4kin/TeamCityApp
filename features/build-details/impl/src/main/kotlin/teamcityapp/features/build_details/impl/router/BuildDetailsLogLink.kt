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

package teamcityapp.features.build_details.impl.router

import android.net.Uri
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject

/** Reads the active account only at launch; browser authentication stays in the browser. */
class BuildDetailsLogLink @Inject constructor(private val storage: SharedUserStorage) {
    fun url(buildId: String): String {
        val account = storage.activeUser
        return Uri.parse(account.teamcityUrl.trimEnd('/') + "/viewLog.html").buildUpon()
            .appendQueryParameter("buildId", buildId)
            .appendQueryParameter("tab", "buildLog")
            .apply { if (account.isGuestUser) appendQueryParameter("guest", "1") }
            .build().toString()
    }
}
